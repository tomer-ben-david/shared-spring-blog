package com.mindmeld360.blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmeld360.blog.config.BlogProperties;
import com.mindmeld360.blog.exception.BlogNotFoundException;
import com.mindmeld360.blog.model.BlogPost;
import com.mindmeld360.blog.service.BlogService;
import com.mindmeld360.blog.util.UrlBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private static final Logger log = LoggerFactory.getLogger(BlogController.class);

    private final BlogService blogService;
    private final BlogProperties blogProperties;
    private final ObjectMapper objectMapper;
    private final UrlBuilder urlBuilder;

    public BlogController(BlogService blogService, BlogProperties blogProperties,
                          ObjectMapper objectMapper, UrlBuilder urlBuilder) {
        this.blogService = blogService;
        this.blogProperties = blogProperties;
        this.objectMapper = objectMapper;
        this.urlBuilder = urlBuilder;
    }

    @GetMapping
    public String index(Model model, HttpServletRequest request) {
        List<BlogPost> posts = blogService.getAllPosts();
        log.info("Blog index request: {} posts found", posts.size());

        String baseUrl = urlBuilder.buildBaseUrl(request);
        String blogUrl = baseUrl + "/blog";

        // Build JSON-LD structured data for blog index
        Map<String, Object> jsonLd = new HashMap<>();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", "Blog");
        jsonLd.put("name", blogProperties.getTitle());
        jsonLd.put("description", blogProperties.getDescription());
        jsonLd.put("url", blogUrl);
        jsonLd.put("publisher", createPublisherJsonLd());

        try {
            model.addAttribute("jsonLd", objectMapper.writeValueAsString(jsonLd));
        } catch (JsonProcessingException e) {
            log.error("Failed to generate JSON-LD for blog index", e);
            model.addAttribute("jsonLd", "{}");
        }

        addCommonAttributes(model);
        model.addAttribute("posts", posts);
        model.addAttribute("pageTitle", blogProperties.getTitle());
        model.addAttribute("metaDescription", blogProperties.getDescription());
        model.addAttribute("canonicalUrl", blogUrl);
        model.addAttribute("ogTitle", blogProperties.getTitle());
        model.addAttribute("ogDescription", blogProperties.getDescription());
        model.addAttribute("ogType", "website");

        return "blog/index";
    }

    @GetMapping("/{slug}")
    public String post(@PathVariable("slug") String slug, Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<BlogPost> postOpt = blogService.getPostBySlug(slug);

        if (postOpt.isEmpty()) {
            log.warn("Blog post not found: {}", slug);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            addCommonAttributes(model);
            model.addAttribute("slug", slug);
            return "blog/not-found";
        }

        BlogPost post = postOpt.get();
        log.info("Blog post request: {}", slug);

        String baseUrl = urlBuilder.buildBaseUrl(request);
        String canonicalUrl = baseUrl + "/blog/" + urlBuilder.encodePathSegment(slug);

        // Handle OG image URL - check if heroImage is already absolute
        String ogImage = null;
        if (post.heroImage() != null && !post.heroImage().isEmpty()) {
            if (post.heroImage().startsWith("http://") || post.heroImage().startsWith("https://")) {
                ogImage = post.heroImage();
            } else {
                ogImage = baseUrl + (post.heroImage().startsWith("/") ? "" : "/") + post.heroImage();
            }
        }

        // Build JSON-LD structured data for blog post
        Map<String, Object> jsonLd = new HashMap<>();
        jsonLd.put("@context", "https://schema.org");
        jsonLd.put("@type", "BlogPosting");
        jsonLd.put("headline", post.title());
        jsonLd.put("description", post.description() != null ? post.description() : "");

        Map<String, Object> author = new HashMap<>();
        author.put("@type", "Person");
        author.put("name", post.author());
        jsonLd.put("author", author);

        jsonLd.put("datePublished", post.pubDate().toString());
        jsonLd.put("dateModified", post.getEffectiveDate().toString());
        jsonLd.put("publisher", createPublisherJsonLd());

        Map<String, Object> mainEntity = new HashMap<>();
        mainEntity.put("@type", "WebPage");
        mainEntity.put("@id", canonicalUrl);
        jsonLd.put("mainEntityOfPage", mainEntity);

        if (ogImage != null) {
            jsonLd.put("image", ogImage);
        }

        try {
            model.addAttribute("jsonLd", objectMapper.writeValueAsString(jsonLd));
        } catch (JsonProcessingException e) {
            log.error("Failed to generate JSON-LD for blog post", e);
            model.addAttribute("jsonLd", "{}");
        }

        addCommonAttributes(model);
        model.addAttribute("post", post);
        model.addAttribute("pageTitle", post.title() + " — " + blogProperties.getTitle());
        model.addAttribute("metaDescription", post.description());
        model.addAttribute("canonicalUrl", canonicalUrl);
        model.addAttribute("ogTitle", post.title());
        model.addAttribute("ogDescription", post.description());
        model.addAttribute("ogType", "article");
        model.addAttribute("ogImage", ogImage);
        model.addAttribute("articlePublishedTime", post.pubDate().toString());
        model.addAttribute("articleAuthor", post.author());

        return "blog/post";
    }

    @ExceptionHandler(BlogNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(BlogNotFoundException ex, Model model) {
        addCommonAttributes(model);
        model.addAttribute("slug", ex.getSlug());
        return "blog/not-found";
    }

    private Map<String, Object> createPublisherJsonLd() {
        Map<String, Object> publisher = new HashMap<>();
        publisher.put("@type", "Organization");
        publisher.put("name", blogProperties.getPublisherName() != null ? blogProperties.getPublisherName() : blogProperties.getTitle());
        publisher.put("url", blogProperties.getPublisherUrl());
        return publisher;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("blogTitle", blogProperties.getTitle());
        model.addAttribute("blogDescription", blogProperties.getDescription());
        model.addAttribute("publisherUrl", blogProperties.getPublisherUrl());
        model.addAttribute("publisherName", blogProperties.getPublisherName());
        model.addAttribute("disqusEnabled", blogProperties.getDisqus().isEnabled());
        model.addAttribute("disqusShortname", blogProperties.getDisqus().getShortname());
        model.addAttribute("socialSharingEnabled", blogProperties.getSocialSharing().isEnabled());
        model.addAttribute("mediumUrl", blogProperties.getMediumUrl());
    }
}
