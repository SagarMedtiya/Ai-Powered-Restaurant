---
name: Gourmet Intelligence
colors:
  surface: '#faf8ff'
  surface-dim: '#d7d9e8'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#ebedfc'
  surface-container-high: '#e5e7f7'
  surface-container-highest: '#e0e2f1'
  on-surface: '#181b26'
  on-surface-variant: '#5b403e'
  inverse-surface: '#2d303b'
  inverse-on-surface: '#eef0ff'
  outline: '#906f6d'
  outline-variant: '#e4bdba'
  surface-tint: '#bb1522'
  primary: '#b81120'
  on-primary: '#ffffff'
  primary-container: '#dc3135'
  on-primary-container: '#fffbff'
  inverse-primary: '#ffb3ae'
  secondary: '#5f5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e4e2e1'
  on-secondary-container: '#656464'
  tertiary: '#00685a'
  on-tertiary: '#ffffff'
  tertiary-container: '#008472'
  on-tertiary-container: '#f4fffa'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad7'
  primary-fixed-dim: '#ffb3ae'
  on-primary-fixed: '#410004'
  on-primary-fixed-variant: '#930014'
  secondary-fixed: '#e4e2e1'
  secondary-fixed-dim: '#c8c6c6'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#474747'
  tertiary-fixed: '#7ff7df'
  tertiary-fixed-dim: '#61dac3'
  on-tertiary-fixed: '#00201b'
  on-tertiary-fixed-variant: '#005045'
  background: '#faf8ff'
  on-background: '#181b26'
  surface-variant: '#e0e2f1'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '800'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  max-width: 800px
  container-padding: 1.5rem
  stack-sm: 0.5rem
  stack-md: 1rem
  stack-lg: 2rem
  gutter: 1rem
---

## Brand & Style
The design system focuses on a high-utility, appetizing experience tailored for food discovery. The brand personality is efficient, reliable, and vibrant, mirroring the immediacy of a personal concierge. 

The aesthetic is **Modern Corporate** with a specific focus on **Visual Hierarchy**. It prioritizes high-quality food photography and clear data points (ratings, distance, price). By utilizing a clean white foundation and a singular, energetic primary color, the system ensures that the AI's recommendations remain the focal point without unnecessary visual noise.

## Colors
The palette is dominated by **Deep Red**, chosen to stimulate appetite and signify action. 
- **Primary (#ff4b4b):** Used for key actions, brand touchpoints, and active states.
- **Secondary (#2d2d2d):** Reserved for primary headings and text to ensure high legibility.
- **Neutral (#686b78):** Used for secondary information, meta-data (like addresses), and icons.
- **Surface Subtle (#f8f8f8):** Employed for background fills in input fields or secondary card sections to provide gentle contrast against the white background.

## Typography
The system uses **Inter** exclusively to maintain a systematic and utilitarian feel. 
- **Headlines:** Use tighter letter-spacing and heavier weights to create a strong "Zomato-style" impact.
- **Body:** Standardized at 16px for optimal readability on mobile and web.
- **Labels:** Used for utility tags (e.g., "Open Now", "Trending") with increased medium weights to stand out at smaller sizes.

## Layout & Spacing
This design system utilizes a **Fixed Grid** approach for desktop, constraining the primary content to a **800px center-aligned container**. This focus prevents eye strain and mimics the digestible feed of a mobile app even on large screens.

- **Mobile:** 100% width with 16px side margins.
- **Tablet/Desktop:** Fixed 800px width.
- **Rhythm:** A strict 4px/8px baseline grid ensures vertical consistency between elements. Cards within the feed should be separated by a `stack-lg` (32px) margin to allow each recommendation breathing room.

## Elevation & Depth
Depth is created through **Ambient Shadows** rather than heavy borders. 
- **Level 1 (Resting):** Cards use a very soft, diffused shadow: `0px 4px 12px rgba(0, 0, 0, 0.05)`.
- **Level 2 (Hover/Active):** The shadow tightens and slightly darkens to `0px 8px 24px rgba(0, 0, 0, 0.12)` to indicate interactivity.
- **Surface Tiers:** Backgrounds are kept white (#ffffff), while secondary input areas or inactive chips use a subtle grey (#f8f8f8) to recede.

## Shapes
The shape language is **Rounded**, conveying a friendly and modern accessible feel. 
- **Cards & Containers:** Use `rounded-lg` (16px) to soften the layout.
- **Buttons & Inputs:** Use `rounded-md` (8px) for a precise, clickable appearance.
- **Images:** All restaurant thumbnails must carry the same corner radius as their parent cards to maintain a cohesive silhouette.

## Components
- **Buttons:** Primary buttons are solid Deep Red with white text. No gradients. Secondary buttons use a Subtle Gray background with Primary Red text.
- **Cards:** The core of the experience. Each card features a full-bleed top image, followed by a content area with `stack-md` padding. Information is stacked: Title (Headline-sm), Meta-row (Rating, Distance, Price), and Tags.
- **Chips:** Small, rounded-pill containers used for cuisines or attributes (e.g., "Outdoor Seating"). Use a light gray background with `label-sm` typography.
- **Inputs:** Search bars should be prominent, using a 1px border (#e8e8e8) and a subtle inner shadow to look "inset" yet clean.
- **Ratings:** Use a dedicated "Rating Badge"—a small rounded rectangle with a green background for scores 4.0+, and primary red for anything lower, reinforcing the quality signal.