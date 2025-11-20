import { useEffect } from 'react';

interface StructuredDataProps {
  data: Record<string, any>;
}

export function StructuredData({ data }: StructuredDataProps) {
  useEffect(() => {
    const scriptId = 'structured-data';
    let script = document.getElementById(scriptId) as HTMLScriptElement;

    if (!script) {
      script = document.createElement('script');
      script.id = scriptId;
      script.type = 'application/ld+json';
      document.head.appendChild(script);
    }

    script.textContent = JSON.stringify(data);

    return () => {
      // Cleanup on unmount
      script.remove();
    };
  }, [data]);

  return null;
}

// Predefined schema templates
export const organizationSchema = {
  '@context': 'https://schema.org',
  '@type': 'Organization',
  name: 'VetConnect',
  description: 'Comprehensive platform connecting U.S. military veterans with essential resources including housing, healthcare, education, and mental health services.',
  url: 'https://vetconnect.com',
  logo: 'https://vetconnect.com/logo.png',
  sameAs: [
    // Add your social media profiles
    'https://github.com/cishocksr/vetconnect',
  ],
  contactPoint: {
    '@type': 'ContactPoint',
    contactType: 'Customer Service',
    email: 'support@vetconnect.com',
    availableLanguage: 'English',
  },
};

export const websiteSchema = {
  '@context': 'https://schema.org',
  '@type': 'WebSite',
  name: 'VetConnect',
  url: 'https://vetconnect.com',
  potentialAction: {
    '@type': 'SearchAction',
    target: 'https://vetconnect.com/resources?search={search_term_string}',
    'query-input': 'required name=search_term_string',
  },
};
