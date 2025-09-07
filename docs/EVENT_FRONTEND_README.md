# Event Management Frontend

This document describes the comprehensive event management functionality that has been added to the Weather App frontend.

## Overview

The event management system provides a complete frontend solution for managing events, including:

- Event creation, editing, and deletion
- Calendar view with monthly and weekly layouts
- Advanced search and filtering
- Event statistics and analytics
- Dashboard integration
- Conflict detection for overlapping events

## Components Created

### Services

#### `eventService.js`
- Complete API service for all event operations
- CRUD operations (Create, Read, Update, Delete)
- Search and filtering functionality
- Statistics retrieval
- Conflict checking
- Name availability checking
- Validation helpers
- Date formatting utilities

### Core Components

#### `EventCard.js`
- Display component for individual events
- Supports both full and compact views
- Shows event status, type, and timing
- Includes action buttons (view, edit, delete)
- Weather-aware styling (outdoor/indoor events)

#### `EventForm.js`
- Comprehensive form for creating and editing events
- Real-time validation and error handling
- Conflict detection with visual warnings
- Name availability checking
- Date/time pickers with validation
- Support for all event types and statuses

#### `EventList.js`
- Container component for displaying multiple events
- Sorting and filtering capabilities
- Pagination support
- Different view modes (grid, compact)
- Loading and error states
- Empty state handling

#### `EventModal.js`
- Modal component for event details and confirmations
- View mode for detailed event information
- Delete confirmation with safety checks
- Responsive design
- Participant management display

#### `EventSearch.js`
- Advanced search and filtering interface
- Quick search by name
- Expandable filters panel
- Date range filtering
- Quick filter buttons
- Active filter display

#### `EventCalendar.js`
- Full calendar component with month/week views
- Event visualization with color coding
- Interactive date selection
- Event creation from calendar clicks
- Navigation controls
- Status-based color coding

### Dashboard Components

#### `UpcomingEvents.js`
- Dashboard widget for upcoming events
- Today's events highlighting
- This week's events section
- Quick action buttons
- Configurable display limits

#### `EventStats.js`
- Statistics dashboard for events
- Event counts by status and type
- Detailed analytics view
- Trend indicators
- Popular event types
- Next event preview

#### `StatCard.js`
- Updated statistics card component
- Color-coded by type
- Trend indicators
- Responsive design
- Consistent styling

### Pages

#### `Events.js`
- Main event management page
- List and compact view toggle
- Create, edit, delete functionality
- Search integration
- Statistics overview
- Modal management

#### `Calendar.js`
- Dedicated calendar page
- Full calendar view
- Event creation from date selection
- Quick statistics
- Loading and error handling

#### `EnhancedDashboard.js`
- Updated dashboard with event integration
- Weather information preserved
- Event statistics cards
- Upcoming events sidebar
- Quick actions panel

## Features

### Event Management
- ✅ Create new events with validation
- ✅ Edit existing events
- ✅ Delete events with confirmation
- ✅ View detailed event information
- ✅ Conflict detection for overlapping events
- ✅ Name availability checking

### Calendar Functionality
- ✅ Monthly calendar view
- ✅ Weekly calendar view
- ✅ Event visualization with color coding
- ✅ Interactive date selection
- ✅ Navigation between months/weeks
- ✅ Today highlighting

### Search and Filtering
- ✅ Quick search by event name
- ✅ Advanced filtering by type, status, location
- ✅ Date range filtering
- ✅ Quick filter buttons
- ✅ Active filter display

### Statistics and Analytics
- ✅ Event count by status
- ✅ Event count by type
- ✅ Location type breakdown (indoor/outdoor)
- ✅ Upcoming events counter
- ✅ This week/month counters
- ✅ Average events per week
- ✅ Most popular event type

### Dashboard Integration
- ✅ Upcoming events widget
- ✅ Event statistics cards
- ✅ Quick action buttons
- ✅ Navigation integration
- ✅ Weather information preserved

### User Experience
- ✅ Responsive design for all screen sizes
- ✅ Loading states and error handling
- ✅ Form validation with real-time feedback
- ✅ Modal interfaces for actions
- ✅ Consistent styling and theming
- ✅ Accessibility considerations

## Backend Integration

The frontend integrates with the following backend endpoints:

### Events API (`/api/v1/events`)
- `GET /` - Get paginated user events
- `GET /all` - Get all user events
- `GET /{id}` - Get event by ID
- `POST /` - Create new event
- `PUT /{id}` - Update event
- `DELETE /{id}` - Delete event
- `POST /search` - Search events
- `GET /upcoming` - Get upcoming events
- `GET /stats` - Get user statistics
- `GET /conflicts` - Check for conflicts
- `GET /check-name` - Check name availability

### Data Models

#### Event Object
```javascript
{
  id: number,
  eventName: string,
  eventDescription: string,
  eventType: 'CONCERT' | 'CONFERENCE' | 'MEETING' | 'WORKSHOP' | 'SPORTS' | 'FESTIVAL' | 'OTHER',
  startDate: string, // ISO date
  endDate: string, // ISO date
  isOutdoor: boolean,
  eventStatus: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED',
  users: Array<User>,
  createdAt: string,
  updatedAt: string
}
```

## Routing

The following routes have been added:

- `/events` - Main event management page
- `/calendar` - Calendar view page
- `/dashboard` - Enhanced dashboard with event integration
- `/dashboard/original` - Original dashboard (fallback)

## Styling

The components use Tailwind CSS for styling with:
- Consistent color scheme
- Responsive design patterns
- Hover and focus states
- Loading and error states
- Status-based color coding

## Usage Examples

### Creating an Event
1. Navigate to `/events`
2. Click "Create Event" button
3. Fill out the form with event details
4. System checks for conflicts automatically
5. Submit to create the event

### Viewing Events in Calendar
1. Navigate to `/calendar`
2. Use month/week toggle to change view
3. Click on dates to create events
4. Click on events to view details
5. Navigate using arrow buttons or "Today" button

### Dashboard Overview
1. Navigate to `/dashboard`
2. View upcoming events in the sidebar
3. See event statistics in the main area
4. Use quick action buttons for common tasks

## Error Handling

- Network errors are caught and displayed to users
- Form validation prevents invalid submissions
- Loading states show during API calls
- Empty states guide users to create content
- Conflict warnings help prevent scheduling issues

## Future Enhancements

Potential improvements that could be added:

1. **Real-time Updates**: WebSocket integration for live updates
2. **Event Sharing**: Share events with other users
3. **Event Templates**: Save and reuse event configurations
4. **Bulk Operations**: Select and perform actions on multiple events
5. **Export Functionality**: Export events to calendar formats
6. **Reminders**: Email/push notification system
7. **Weather Integration**: Show weather for outdoor events
8. **Location Integration**: Map integration for event locations
9. **Recurring Events**: Support for repeating events
10. **Event Analytics**: More detailed reporting and charts

## Dependencies

The event functionality relies on:
- React Router for navigation
- Axios for HTTP requests
- Tailwind CSS for styling
- Date formatting utilities
- Form validation helpers

All components are self-contained and can be used independently or together as a complete event management system.
