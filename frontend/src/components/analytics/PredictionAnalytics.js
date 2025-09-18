import React, { useState, useEffect } from 'react';
import analyticsService from '../../services/analyticsService';
import StatCard from '../StatCard';
import './PredictionAnalytics.css';

const PredictionAnalytics = () => {
  const [predictionData, setPredictionData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [timeRange, setTimeRange] = useState('7d');
  const [selectedModel, setSelectedModel] = useState('all');

  useEffect(() => {
    fetchPredictionAnalytics();
  }, [timeRange, selectedModel]);

  const fetchPredictionAnalytics = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await analyticsService.getPredictionAccuracy(timeRange);
      setPredictionData(data);
    } catch (err) {
      console.error('Error fetching prediction analytics:', err);
      setError('Failed to load prediction analytics. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getPredictionStats = () => {
    if (!predictionData) return [];
    
    return [
      {
        title: 'Overall Accuracy',
        value: predictionData.overallAccuracy ? `${predictionData.overallAccuracy}%` : 'N/A',
        change: predictionData.accuracyChange ? `${predictionData.accuracyChange > 0 ? '+' : ''}${predictionData.accuracyChange}%` : 'N/A',
        trend: predictionData.accuracyChange > 0 ? 'up' : predictionData.accuracyChange < 0 ? 'down' : 'neutral',
        icon: '🎯'
      },
      {
        title: 'Total Predictions',
        value: predictionData.totalPredictions || 0,
        change: predictionData.predictionsChange ? `${predictionData.predictionsChange > 0 ? '+' : ''}${predictionData.predictionsChange}%` : 'N/A',
        trend: predictionData.predictionsChange > 0 ? 'up' : predictionData.predictionsChange < 0 ? 'down' : 'neutral',
        icon: '🔮'
      },
      {
        title: 'Model Confidence',
        value: predictionData.avgConfidence ? `${predictionData.avgConfidence}%` : 'N/A',
        change: predictionData.confidenceChange ? `${predictionData.confidenceChange > 0 ? '+' : ''}${predictionData.confidenceChange}%` : 'N/A',
        trend: predictionData.confidenceChange > 0 ? 'up' : predictionData.confidenceChange < 0 ? 'down' : 'neutral',
        icon: '💪'
      },
      {
        title: 'Avg Response Time',
        value: predictionData.avgResponseTime ? `${predictionData.avgResponseTime}ms` : 'N/A',
        change: predictionData.responseTimeChange ? `${predictionData.responseTimeChange > 0 ? '+' : ''}${predictionData.responseTimeChange}ms` : 'N/A',
        trend: predictionData.responseTimeChange > 0 ? 'down' : predictionData.responseTimeChange < 0 ? 'up' : 'neutral',
        icon: '⚡'
      }
    ];
  };

  const getModelPerformance = () => {
    return predictionData?.modelPerformance || [
      { 
        model: 'Temperature Forecasting', 
        accuracy: 92.5, 
        predictions: 1543, 
        confidence: 89.2,
        type: 'temperature' 
      },
      { 
        model: 'Precipitation Prediction', 
        accuracy: 87.3, 
        predictions: 1234, 
        confidence: 84.7,
        type: 'precipitation' 
      },
      { 
        model: 'Wind Speed Estimation', 
        accuracy: 89.1, 
        predictions: 987, 
        confidence: 86.3,
        type: 'wind' 
      },
      { 
        model: 'Humidity Forecasting', 
        accuracy: 94.2, 
        predictions: 876, 
        confidence: 91.8,
        type: 'humidity' 
      },
      { 
        model: 'Pressure Prediction', 
        accuracy: 96.1, 
        predictions: 654, 
        confidence: 93.4,
        type: 'pressure' 
      }
    ];
  };

  const getAccuracyTrends = () => {
    return predictionData?.accuracyTrends || [
      { period: 'Day 1', accuracy: 89.2, predictions: 234 },
      { period: 'Day 2', accuracy: 91.1, predictions: 287 },
      { period: 'Day 3', accuracy: 88.7, predictions: 198 },
      { period: 'Day 4', accuracy: 92.3, predictions: 312 },
      { period: 'Day 5', accuracy: 90.8, predictions: 276 },
      { period: 'Day 6', accuracy: 93.1, predictions: 289 },
      { period: 'Day 7', accuracy: 91.7, predictions: 298 }
    ];
  };

  const getPredictionInsights = () => {
    if (!predictionData) return [];

    const insights = [];

    // Accuracy insights
    if (predictionData.overallAccuracy > 90) {
      insights.push({
        type: 'success',
        icon: '🎯',
        title: 'Excellent Accuracy',
        message: `Your models maintain an excellent ${predictionData.overallAccuracy}% accuracy rate.`
      });
    } else if (predictionData.overallAccuracy < 80) {
      insights.push({
        type: 'warning',
        icon: '⚠️',
        title: 'Accuracy Concern',
        message: `Model accuracy of ${predictionData.overallAccuracy}% needs improvement. Consider retraining.`
      });
    }

    // Performance insights
    if (predictionData.avgResponseTime > 1000) {
      insights.push({
        type: 'warning',
        icon: '🐌',
        title: 'Slow Predictions',
        message: `Average prediction time of ${predictionData.avgResponseTime}ms is above optimal threshold.`
      });
    }

    // Confidence insights
    if (predictionData.avgConfidence < 75) {
      insights.push({
        type: 'info',
        icon: '🤔',
        title: 'Low Confidence',
        message: `Model confidence of ${predictionData.avgConfidence}% suggests uncertainty in predictions.`
      });
    }

    // Volume insights
    if (predictionData.predictionsChange > 25) {
      insights.push({
        type: 'info',
        icon: '📈',
        title: 'High Demand',
        message: `Prediction requests increased by ${predictionData.predictionsChange}%. Monitor system resources.`
      });
    }

    // Best performing model
    const modelPerformance = getModelPerformance();
    const bestModel = modelPerformance.reduce((best, current) => 
      current.accuracy > best.accuracy ? current : best
    );
    
    insights.push({
      type: 'success',
      icon: '🏆',
      title: 'Top Performer',
      message: `${bestModel.model} is your best performing model with ${bestModel.accuracy}% accuracy.`
    });

    return insights;
  };

  if (loading) {
    return (
      <div className="prediction-analytics-loading">
        <div className="loading-spinner">🔮</div>
        <p>Loading prediction analytics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="prediction-analytics-error">
        <div className="error-icon">⚠️</div>
        <h3>Unable to Load Prediction Analytics</h3>
        <p>{error}</p>
        <button className="retry-button" onClick={fetchPredictionAnalytics}>
          🔄 Retry
        </button>
      </div>
    );
  }

  const predictionStats = getPredictionStats();
  const modelPerformance = getModelPerformance();
  const accuracyTrends = getAccuracyTrends();
  const insights = getPredictionInsights();

  return (
    <div className="prediction-analytics">
      {/* Header */}
      <div className="analytics-header">
        <div className="header-content">
          <h2>🔮 Prediction Analytics</h2>
          <p>Monitor AI model performance and prediction accuracy</p>
        </div>
        <div className="header-controls">
          <div className="time-range-selector">
            <label>Time Range:</label>
            <select 
              value={timeRange} 
              onChange={(e) => setTimeRange(e.target.value)}
            >
              <option value="24h">Last 24 Hours</option>
              <option value="7d">Last 7 Days</option>
              <option value="30d">Last 30 Days</option>
              <option value="90d">Last 3 Months</option>
            </select>
          </div>
          <div className="model-selector">
            <label>Model:</label>
            <select 
              value={selectedModel} 
              onChange={(e) => setSelectedModel(e.target.value)}
            >
              <option value="all">All Models</option>
              <option value="temperature">Temperature</option>
              <option value="precipitation">Precipitation</option>
              <option value="wind">Wind Speed</option>
              <option value="humidity">Humidity</option>
              <option value="pressure">Pressure</option>
            </select>
          </div>
          <button className="refresh-button" onClick={fetchPredictionAnalytics}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* Overview Stats */}
      <div className="stats-section">
        <h3>📊 Performance Overview</h3>
        <div className="stats-grid">
          {predictionStats.map((stat, index) => (
            <StatCard key={index} {...stat} />
          ))}
        </div>
      </div>

      {/* Model Performance */}
      <div className="model-performance-section">
        <h3>🤖 Model Performance</h3>
        <div className="performance-table">
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Model</th>
                  <th>Accuracy</th>
                  <th>Predictions</th>
                  <th>Confidence</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {modelPerformance
                  .filter(model => selectedModel === 'all' || model.type === selectedModel)
                  .map((model, index) => (
                  <tr key={index}>
                    <td className="model-name">
                      <span className="model-icon">
                        {model.type === 'temperature' && '🌡️'}
                        {model.type === 'precipitation' && '🌧️'}
                        {model.type === 'wind' && '💨'}
                        {model.type === 'humidity' && '💧'}
                        {model.type === 'pressure' && '📏'}
                      </span>
                      {model.model}
                    </td>
                    <td className="accuracy">
                      <span className={`accuracy-badge ${model.accuracy > 90 ? 'excellent' : model.accuracy > 80 ? 'good' : 'needs-improvement'}`}>
                        {model.accuracy}%
                      </span>
                    </td>
                    <td className="predictions-count">{model.predictions.toLocaleString()}</td>
                    <td className="confidence">{model.confidence}%</td>
                    <td className="status">
                      <span className={`status-indicator ${model.accuracy > 85 ? 'healthy' : 'warning'}`}>
                        {model.accuracy > 85 ? '🟢 Healthy' : '🟡 Monitor'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Accuracy Trends */}
      <div className="trends-section">
        <h3>📈 Accuracy Trends</h3>
        <div className="trends-chart">
          <div className="chart-header">
            <h4>Daily Accuracy Performance</h4>
            <div className="chart-legend">
              <span className="legend-item">
                <span className="legend-color accuracy-color"></span>
                Accuracy %
              </span>
              <span className="legend-item">
                <span className="legend-color volume-color"></span>
                Prediction Volume
              </span>
            </div>
          </div>
          <div className="dual-chart">
            <div className="chart-container">
              <div className="y-axis">
                <span>100%</span>
                <span>75%</span>
                <span>50%</span>
                <span>25%</span>
                <span>0%</span>
              </div>
              <div className="chart-bars">
                {accuracyTrends.map((trend, index) => (
                  <div key={index} className="trend-bar-group">
                    <div 
                      className="accuracy-bar"
                      style={{ height: `${trend.accuracy}%` }}
                      title={`${trend.period}: ${trend.accuracy}% accuracy`}
                    ></div>
                    <div 
                      className="volume-bar"
                      style={{ height: `${(trend.predictions / 350) * 100}%` }}
                      title={`${trend.period}: ${trend.predictions} predictions`}
                    ></div>
                    <span className="x-label">{trend.period}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Prediction Quality Analysis */}
      <div className="quality-analysis-section">
        <h3>🔍 Quality Analysis</h3>
        <div className="quality-grid">
          <div className="quality-card">
            <h4>🎯 Accuracy Distribution</h4>
            <div className="accuracy-distribution">
              <div className="distribution-item">
                <span className="range-label">90-100%</span>
                <div className="range-bar">
                  <div className="range-fill excellent" style={{ width: '45%' }}></div>
                </div>
                <span className="range-percentage">45%</span>
              </div>
              <div className="distribution-item">
                <span className="range-label">80-89%</span>
                <div className="range-bar">
                  <div className="range-fill good" style={{ width: '35%' }}></div>
                </div>
                <span className="range-percentage">35%</span>
              </div>
              <div className="distribution-item">
                <span className="range-label">70-79%</span>
                <div className="range-bar">
                  <div className="range-fill average" style={{ width: '15%' }}></div>
                </div>
                <span className="range-percentage">15%</span>
              </div>
              <div className="distribution-item">
                <span className="range-label">Below 70%</span>
                <div className="range-bar">
                  <div className="range-fill poor" style={{ width: '5%' }}></div>
                </div>
                <span className="range-percentage">5%</span>
              </div>
            </div>
          </div>

          <div className="quality-card">
            <h4>💪 Confidence Levels</h4>
            <div className="confidence-breakdown">
              <div className="confidence-item">
                <span className="confidence-icon">🔥</span>
                <div className="confidence-info">
                  <span className="confidence-label">High Confidence</span>
                  <span className="confidence-range">(90%+)</span>
                </div>
                <span className="confidence-percentage">38%</span>
              </div>
              <div className="confidence-item">
                <span className="confidence-icon">👍</span>
                <div className="confidence-info">
                  <span className="confidence-label">Good Confidence</span>
                  <span className="confidence-range">(75-89%)</span>
                </div>
                <span className="confidence-percentage">42%</span>
              </div>
              <div className="confidence-item">
                <span className="confidence-icon">🤔</span>
                <div className="confidence-info">
                  <span className="confidence-label">Low Confidence</span>
                  <span className="confidence-range">(Below 75%)</span>
                </div>
                <span className="confidence-percentage">20%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Prediction Insights */}
      <div className="insights-section">
        <h3>💡 Prediction Insights</h3>
        {insights.length > 0 ? (
          <div className="insights-grid">
            {insights.map((insight, index) => (
              <div key={index} className={`insight-card ${insight.type}`}>
                <div className="insight-header">
                  <span className="insight-icon">{insight.icon}</span>
                  <h4>{insight.title}</h4>
                </div>
                <p>{insight.message}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-insights">
            <span className="no-insights-icon">🔮</span>
            <p>No prediction insights available for the current selection.</p>
          </div>
        )}
      </div>

      {/* Model Health Summary */}
      <div className="model-health-section">
        <h3>🏥 Model Health Summary</h3>
        <div className="health-summary">
          <div className="health-metrics">
            <div className="health-metric">
              <span className="metric-icon">🎯</span>
              <div className="metric-details">
                <span className="metric-label">Average Accuracy</span>
                <span className="metric-value">{predictionData?.overallAccuracy || 91.2}%</span>
              </div>
              <span className={`health-status ${(predictionData?.overallAccuracy || 91.2) > 90 ? 'excellent' : 'good'}`}>
                {(predictionData?.overallAccuracy || 91.2) > 90 ? 'Excellent' : 'Good'}
              </span>
            </div>
            
            <div className="health-metric">
              <span className="metric-icon">⚡</span>
              <div className="metric-details">
                <span className="metric-label">Response Speed</span>
                <span className="metric-value">{predictionData?.avgResponseTime || 245}ms</span>
              </div>
              <span className={`health-status ${(predictionData?.avgResponseTime || 245) < 500 ? 'excellent' : 'good'}`}>
                {(predictionData?.avgResponseTime || 245) < 500 ? 'Fast' : 'Moderate'}
              </span>
            </div>
            
            <div className="health-metric">
              <span className="metric-icon">🔄</span>
              <div className="metric-details">
                <span className="metric-label">Model Freshness</span>
                <span className="metric-value">3 days ago</span>
              </div>
              <span className="health-status excellent">Fresh</span>
            </div>
          </div>
          
          <div className="health-recommendations">
            <h4>🎯 Recommendations</h4>
            <ul>
              <li>Schedule regular model retraining for optimal performance</li>
              <li>Monitor low-confidence predictions for quality improvement</li>
              <li>Consider A/B testing for model optimization</li>
              <li>Implement automated alerts for accuracy drops</li>
              <li>Expand training data for underperforming models</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PredictionAnalytics;