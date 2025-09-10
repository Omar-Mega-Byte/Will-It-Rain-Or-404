# Redis Implementation in Weather App

This document describes the Redis implementation for caching and analytics in the Weather Application.

## Features Implemented

### 1. Weather Data Caching
- **Current Weather Cache**: 5-minute TTL
- **Weather Forecast Cache**: 30-minute TTL  
- **Historical Weather Cache**: 2-hour TTL
- **User Preferences Cache**: 1-hour TTL
- **Weather Alerts Cache**: 1-minute TTL

### 2. Analytics & Tracking
- **API Usage Tracking**: Track all weather API calls
- **Location Popularity**: Most requested weather locations
- **User Activity**: Individual user usage patterns
- **Error Tracking**: Monitor API failures and errors
- **Real-time Analytics**: Hourly and daily statistics

### 3. Health Monitoring
- **Redis Connectivity**: Health check endpoints
- **Cache Statistics**: Monitor cache hit/miss ratios
- **Performance Metrics**: Response time tracking

## API Endpoints

### Cached Weather Endpoints
```
GET /api/weather/cached/current?location=<city>
GET /api/weather/cached/forecast?location=<city>&days=7  
GET /api/weather/cached/historical?location=<city>&startDate=2025-01-01&endDate=2025-01-31
GET /api/weather/cached/popular-locations?limit=10
GET /api/weather/cached/health
```

### User Preferences
```
POST /api/weather/cached/preferences
GET /api/weather/cached/preferences
```

### Weather Alerts
```
POST /api/weather/cached/alerts/{location} (Admin only)
GET /api/weather/cached/alerts/{location}
```

### Analytics Endpoints
```
GET /api/weather/analytics/dashboard (Admin only)
GET /api/weather/analytics/daily?date=2025-09-10 (Admin only)
GET /api/weather/analytics/hourly (Admin only)
GET /api/weather/analytics/endpoints/top?limit=10 (Admin only)
GET /api/weather/analytics/locations/top?limit=10 (Admin only)
GET /api/weather/analytics/user/me
GET /api/weather/analytics/errors?date=2025-09-10 (Admin only)
```

### Cache Management
```
DELETE /api/weather/cached/cache/{location} (Admin only)
GET /api/weather/cached/cache/stats (Admin only)
```

## Configuration

### Redis Configuration
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

### Cache TTL Settings
- Current Weather: 5 minutes
- Weather Forecast: 30 minutes  
- Historical Weather: 2 hours
- User Preferences: 1 hour
- Weather Alerts: 1 minute

## Key Components

### 1. RedisConfig
- Configures Redis templates and serializers
- Sets up cache managers with custom TTL
- JSON serialization for complex objects

### 2. WeatherCacheService
- Main caching service for weather data
- Implements @Cacheable annotations
- Manages popular locations tracking
- Handles cache eviction

### 3. WeatherAnalyticsService
- Tracks API usage patterns
- Stores analytics in Redis sorted sets
- Provides comprehensive reporting
- Async processing for performance

### 4. CachedWeatherController
- REST endpoints with caching
- Automatic popularity tracking
- Health monitoring endpoints
- User preference management

### 5. WeatherAnalyticsController
- Analytics dashboard endpoints
- Real-time usage statistics
- Error monitoring and reporting
- User activity tracking

### 6. WeatherTrackingAspect
- Automatic request tracking using AOP
- Tracks both successful and failed calls
- Extracts metadata from requests
- Non-intrusive monitoring

## Redis Data Structure

### Cache Keys
```
weather:current:{location}           - Current weather data
weather:forecast:{location}:{days}   - Weather forecasts
weather:historical:{location}:{date} - Historical data
weather:preferences:{userId}         - User preferences
weather:alerts:{location}           - Weather alerts
```

### Analytics Keys
```
analytics:daily:requests:{date}      - Daily request counts
analytics:hourly:requests:{hour}     - Hourly request counts
analytics:endpoints:usage            - Endpoint usage (sorted set)
analytics:locations:requests         - Location popularity (sorted set)
analytics:user:activity:{userId}     - User activity (sorted set)
analytics:errors:{date}:{type}       - Error tracking
```

### Popular Locations
```
weather:popular:locations           - Popular locations (sorted set)
```

## Benefits

1. **Performance Improvement**: Reduced API response times through caching
2. **Reduced External API Calls**: Lower costs and rate limiting issues
3. **Real-time Analytics**: Immediate insights into usage patterns
4. **Scalability**: Redis clustering support for high availability
5. **Monitoring**: Comprehensive health checks and metrics
6. **User Experience**: Faster responses and personalized preferences

## Usage Examples

### Getting Current Weather (Cached)
```bash
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/weather/cached/current?location=New York"
```

### Viewing Analytics Dashboard
```bash
curl -H "Authorization: Bearer <admin-token>" \
     "http://localhost:8080/api/weather/analytics/dashboard"
```

### Checking Cache Health
```bash
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/weather/cached/health"
```

## Redis Setup

1. Install Redis locally or use Docker:
```bash
docker run -d -p 6379:6379 redis:alpine
```

2. Update application.yml if using different host/port

3. Start the application - Redis integration is automatic

## Monitoring

The implementation includes:
- Health check endpoints
- Cache statistics
- Error rate monitoring  
- Response time tracking
- Usage analytics dashboard

Access the analytics dashboard at `/api/weather/analytics/dashboard` (Admin role required).

## Security

- All endpoints require authentication
- Analytics endpoints require Admin role
- Cache management requires Admin role
- User preferences are user-specific

## Future Enhancements

1. **Distributed Caching**: Redis Cluster support
2. **Cache Warming**: Preload popular locations
3. **Advanced Analytics**: Machine learning insights
4. **Cache Optimization**: Dynamic TTL based on usage
5. **Real-time Notifications**: WebSocket integration for alerts
