import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

interface MetaTagsProps {
  title?: string;
  description?: string;
  keywords?: string;
  ogImage?: string;
  ogType?: string;
  canonicalUrl?: string;
}

const DEFAULT_META = {
  title: 'VetConnect - Veteran Resource Platform | Housing, Healthcare & Education',
  description: 'VetConnect connects U.S. military veterans with essential resources including housing assistance, healthcare, mental health services, education programs, and financial aid. Free veteran support platform.',
  keywords: 'veterans resources, veteran housing, VA benefits, veteran healthcare, GI Bill, PTSD treatment, veteran mental health, military benefits, veteran assistance, veteran support',
  ogImage: '/og-image.jpg',
  siteUrl: 'https://vetconnect.com', // Update with your actual domain
};

export function MetaTags({
  title,
  description,
  keywords,
  ogImage,
  ogType = 'website',
  canonicalUrl,
}: MetaTagsProps) {
  const location = useLocation();

  const finalTitle = title ? `${title} | VetConnect` : DEFAULT_META.title;
  const finalDescription = description || DEFAULT_META.description;
  const finalKeywords = keywords || DEFAULT_META.keywords;
  const finalOgImage = ogImage || DEFAULT_META.ogImage;
  const finalCanonicalUrl = canonicalUrl || `${DEFAULT_META.siteUrl}${location.pathname}`;

  useEffect(() => {
    // Update document title
    document.title = finalTitle;

    // Update or create meta tags
    updateMetaTag('description', finalDescription);
    updateMetaTag('keywords', finalKeywords);
    
    // Open Graph tags
    updateMetaTag('og:title', finalTitle, 'property');
    updateMetaTag('og:description', finalDescription, 'property');
    updateMetaTag('og:image', finalOgImage, 'property');
    updateMetaTag('og:url', finalCanonicalUrl, 'property');
    updateMetaTag('og:type', ogType, 'property');
    updateMetaTag('og:site_name', 'VetConnect', 'property');
    
    // Twitter Card tags
    updateMetaTag('twitter:card', 'summary_large_image', 'name');
    updateMetaTag('twitter:title', finalTitle, 'name');
    updateMetaTag('twitter:description', finalDescription, 'name');
    updateMetaTag('twitter:image', finalOgImage, 'name');

    // Canonical URL
    updateLinkTag('canonical', finalCanonicalUrl);
  }, [finalTitle, finalDescription, finalKeywords, finalOgImage, finalCanonicalUrl, ogType]);

  return null;
}

function updateMetaTag(name: string, content: string, attribute: 'name' | 'property' = 'name') {
  let element = document.querySelector(`meta[${attribute}="${name}"]`);
  
  if (!element) {
    element = document.createElement('meta');
    element.setAttribute(attribute, name);
    document.head.appendChild(element);
  }
  
  element.setAttribute('content', content);
}

function updateLinkTag(rel: string, href: string) {
  let element = document.querySelector(`link[rel="${rel}"]`) as HTMLLinkElement;
  
  if (!element) {
    element = document.createElement('link');
    element.setAttribute('rel', rel);
    document.head.appendChild(element);
  }
  
  element.setAttribute('href', href);
}
