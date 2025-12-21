# Shared Spring Blog

A reusable Spring Boot library for adding blog functionality to your applications. Provides markdown-based blogging with YAML front matter, Disqus comments, social sharing, and RSS feeds.

## Features

- **Markdown Blog Posts** - Write posts in markdown with YAML front matter
- **Thymeleaf Templates** - Pre-built templates for blog index, individual posts, and 404 pages
- **SEO Optimized** - JSON-LD structured data, Open Graph tags, canonical URLs
- **Hero Images** - Optional hero/featured images for posts
- **Disqus Comments** - Built-in comment integration (enabled by default)
- **Social Sharing** - Twitter, LinkedIn, Facebook, Medium share buttons
- **RSS Feed** - Auto-generated RSS feed at `/blog/rss.xml`
- **Draft Support** - Keep posts in draft until ready to publish
- **Proxy-Aware URLs** - Works correctly behind reverse proxies (Nginx, Traefik, etc.)

## Requirements

- Java 17+
- Spring Boot 3.2+

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.mindmeld360</groupId>
    <artifactId>devx-spring-blog</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Import the Configuration

```java
@Import(BlogConfiguration.class)
@SpringBootApplication
public class MyApp { }
```

### 2. Configure Properties

Add to `application.properties`:

```properties
# Required
blog.title=My Blog
blog.description=Thoughts on software development
blog.publisher-url=https://example.com
blog.publisher-name=Example Inc

# Optional
blog.default-author=John Doe
blog.disqus.shortname=my-disqus-site
blog.disqus.enabled=true
blog.social-sharing.enabled=true
blog.rss.max-items=20
blog.content-path=content/blog
```

### 3. Create Required Fragments

The library templates expect these fragments in your project:

```html
<!-- templates/fragments/head.html -->
<head th:fragment="head">
    <meta charset="UTF-8">
    <title th:text="${blogTitle}">Blog</title>
    <link rel="stylesheet" th:href="@{/css/shared-blog.css}">
</head>

<!-- templates/fragments/header.html -->
<header th:fragment="header">
    <nav>Your site navigation here</nav>
</header>

<!-- templates/fragments/footer.html -->
<footer th:fragment="footer">
    <p>Your site footer here</p>
</footer>
```

### 4. Add Blog Posts

Create markdown files in `src/main/resources/content/blog/`:

```markdown
---
title: "My First Post"
description: "An introduction to my blog"
pubDate: 2025-12-14
updatedDate: 2025-12-15
heroImage: "/images/blog/my-first-post.jpg"
author: "John Doe"
tags: ["Introduction", "Welcome"]
draft: false
---

# Welcome to My Blog

This is the content of my first post written in **markdown**.
```

The filename becomes the URL slug: `my-first-post.md` → `/blog/my-first-post`

## Configuration Reference

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `blog.title` | Yes | - | Blog title for templates and RSS |
| `blog.description` | Yes | - | Blog description for SEO and RSS |
| `blog.publisher-url` | Yes | - | Base URL for canonical links |
| `blog.publisher-name` | No | - | Publisher name (for consumer fragments) |
| `blog.default-author` | No | - | Fallback author when post has none |
| `blog.content-path` | No | `content/blog` | Classpath location for markdown files |
| `blog.disqus.enabled` | No | `true` | Enable Disqus comments |
| `blog.disqus.shortname` | No | - | Disqus site shortname |
| `blog.social-sharing.enabled` | No | `true` | Enable social share buttons |
| `blog.medium-url` | No | - | Medium profile URL for sharing |
| `blog.rss.max-items` | No | `20` | Max items in RSS feed |

## Blog Post Front Matter

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `title` | Yes | String | Post title |
| `pubDate` | Yes | Date | Publication date (YYYY-MM-DD) |
| `description` | No | String | Short description for SEO/RSS |
| `updatedDate` | No | Date | Last update date (YYYY-MM-DD), shown if different from pubDate |
| `heroImage` | No | String | Hero/featured image URL (relative or absolute) |
| `author` | No | String | Post author (falls back to `blog.default-author`) |
| `tags` | No | List | Tags for categorization |
| `draft` | No | Boolean | If true, post is hidden from listing |

## URLs

| URL | Description |
|-----|-------------|
| `/blog` | Blog index page |
| `/blog/{slug}` | Individual post page |
| `/blog/rss.xml` | RSS feed |

## Customizing Templates

Override any template by creating your own version in `templates/blog/`:

- `templates/blog/index.html` - Blog listing page
- `templates/blog/post.html` - Individual post page
- `templates/blog/not-found.html` - 404 page

## CSS Classes

All CSS classes are prefixed with `sb-` (shared-blog) to avoid conflicts:

- `.sb-blog-index` - Blog index container
- `.sb-blog-post` - Individual post container
- `.sb-post-title` - Post title
- `.sb-post-meta` - Post metadata (date, author, tags)
- `.sb-post-content` - Post content area
- `.sb-social-sharing` - Social share buttons container
- `.sb-tag` - Individual tag

## Security

> **IMPORTANT:** This library is designed for **trusted, author-controlled content only**.

### Security Model

| Content Type | Protection | Notes |
|--------------|------------|-------|
| Post body (HTML) | **None** - rendered with `th:utext` | Raw HTML allowed for author flexibility |
| Metadata (title, description) | Escaped with `th:text` | Safe from XSS |
| URLs in markdown | `sanitizeUrls(true)` | Blocks `javascript:` and `data:` schemes |

### Why Raw HTML is Allowed

The library uses CommonMark with `escapeHtml(false)` to allow:
- Embedded videos (`<iframe>`)
- Custom HTML widgets
- Advanced formatting

This is **safe** because:
1. Markdown files are stored in your classpath (controlled by developers)
2. Only trusted authors can create/modify posts
3. There's no user-generated content pathway

### When NOT to Use This Library

Do NOT use this library if:
- Users can submit or edit blog posts
- Content comes from untrusted sources
- You need a CMS with user authentication

For user-generated content, use a sanitization library like [OWASP Java HTML Sanitizer](https://github.com/OWASP/java-html-sanitizer).

### Recommendations

1. **Keep markdown files in version control** - changes are auditable
2. **Use code review for post changes** - catch malicious content before deployment
3. **Don't expose content editing endpoints** - this is a static blog library

## License

MIT License - see [LICENSE](LICENSE) file.
