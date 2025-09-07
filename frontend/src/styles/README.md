# Weather App Frontend CSS Documentation

This directory contains all the CSS styles for the Weather App frontend. The styles are organized into pages and components for maintainability and reusability.

## Structure

```
src/
├── index.css                      # Global base styles and CSS reset
├── App.css                        # Main app styles (existing)
├── pages/                         # Page-specific styles
│   ├── Dashboard.css             # Dashboard page styles
│   ├── EnhancedDashboard.css     # Enhanced dashboard styles
│   ├── Events.css                # Events page styles
│   ├── Calendar.css              # Calendar page styles
│   ├── Home.css                  # Home page styles (existing)
│   ├── Login.css                 # Login page styles (existing)
│   └── Register.css              # Register page styles (existing)
└── styles/
    ├── index.css                 # Master import file for all styles
    └── components/               # Reusable component styles
        ├── Common.css            # Shared utilities and base components
        ├── EventCard.css         # Event card component
        ├── EventForm.css         # Event form component
        ├── EventModal.css        # Event modal component
        └── EventSearch.css       # Event search component
```

## Usage

### Method 1: Import Individual CSS Files
Import specific CSS files in your React components:

```jsx
// In Dashboard.js
import '../pages/Dashboard.css';

// In EventCard component
import '../styles/components/EventCard.css';
```

### Method 2: Import Master CSS File
Import the master CSS file that includes all styles:

```jsx
// In App.js or index.js
import './styles/index.css';
```

### Method 3: Use Existing App.css Structure
The existing App.css already contains comprehensive styles. You can either:
1. Replace it with the new structure
2. Append the new styles to the existing file
3. Import additional CSS files as needed

## Design System

The CSS follows a consistent design system with:

### Color Palette
- **Primary**: Blue (#3b82f6 to #1d4ed8)
- **Success**: Green (#10b981 to #059669) 
- **Warning**: Yellow (#f59e0b to #d97706)
- **Error**: Red (#ef4444 to #dc2626)
- **Neutral**: Gray scale (#f9fafb to #111827)

### Typography
- **Font Family**: Segoe UI, Tahoma, Geneva, Verdana, sans-serif
- **Font Sizes**: xs(0.75rem) to 4xl(2.25rem)
- **Font Weights**: 400(normal), 500(medium), 600(semibold), 700(bold)

### Spacing System
- Uses consistent spacing scale from 0.25rem to 4rem
- Grid-based layouts with responsive breakpoints

### Border Radius
- **Small**: 0.25rem
- **Default**: 0.5rem
- **Medium**: 0.75rem
- **Large**: 1rem
- **Extra Large**: 1.5rem+

### Shadows
- Consistent shadow system from subtle (sm) to dramatic (xl)
- Used for depth and hierarchy

## Components

### Page Components
1. **Dashboard** - Weather dashboard with cards and stats
2. **Enhanced Dashboard** - Modern dashboard with navigation
3. **Events** - Event management with list/grid views
4. **Calendar** - Calendar grid with event display

### UI Components
1. **EventCard** - Reusable event display card
2. **EventForm** - Form for creating/editing events
3. **EventModal** - Modal for event actions
4. **EventSearch** - Search and filter functionality
5. **Common** - Shared utilities (buttons, alerts, pagination, etc.)

## Responsive Design

All components are fully responsive with breakpoints:
- **Mobile**: < 480px
- **Tablet**: 480px - 768px  
- **Desktop**: 768px - 1024px
- **Large Desktop**: > 1024px

## Browser Support

Styles are designed to work in:
- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- iOS Safari
- Chrome Mobile

## CSS Features Used

- CSS Grid and Flexbox for layouts
- CSS Custom Properties (CSS Variables)
- CSS Animations and Transitions
- Backdrop-filter for modern effects
- Gradient backgrounds
- Modern selectors and pseudo-classes

## Performance Considerations

- Minimal CSS bundle size through targeted imports
- Efficient selectors and minimal nesting
- Hardware-accelerated animations using transform and opacity
- Reduced motion support for accessibility

## Customization

To customize the design system:

1. **Colors**: Update CSS custom properties in `index.css`
2. **Typography**: Modify font variables and base styles
3. **Spacing**: Adjust spacing scale variables
4. **Components**: Override component-specific styles

## Best Practices

1. Use CSS custom properties for consistent theming
2. Follow BEM-like naming conventions
3. Keep specificity low and avoid deep nesting
4. Use semantic color names (primary, success, etc.)
5. Maintain responsive design principles
6. Test across different browsers and devices

## Integration with React

These CSS files work seamlessly with your existing React components. Simply import the required CSS files and use the class names in your JSX:

```jsx
<div className="event-card">
  <div className="event-card-header">
    <h3 className="event-card-title">Event Title</h3>
    <span className="event-card-status scheduled">Scheduled</span>
  </div>
  <div className="event-card-body">
    <p className="event-card-description">Event description...</p>
  </div>
</div>
```
