import React from 'react';

const StatCard = ({ 
  title, 
  value, 
  icon, 
  color = 'blue', 
  subtitle, 
  trend,
  className = '' 
}) => {
  const getColorClasses = (color) => {
    const colors = {
      blue: 'bg-blue-50 text-blue-700 border-blue-200',
      green: 'bg-green-50 text-green-700 border-green-200',
      yellow: 'bg-yellow-50 text-yellow-700 border-yellow-200',
      purple: 'bg-purple-50 text-purple-700 border-purple-200',
      red: 'bg-red-50 text-red-700 border-red-200',
      indigo: 'bg-indigo-50 text-indigo-700 border-indigo-200',
      gray: 'bg-gray-50 text-gray-700 border-gray-200',
    };
    return colors[color] || colors.blue;
  };

  const getTrendIcon = (trend) => {
    if (!trend) return null;
    
    if (trend > 0) {
      return (
        <svg className="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 17l9.2-9.2M17 17V7H7" />
        </svg>
      );
    } else if (trend < 0) {
      return (
        <svg className="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 7l-9.2 9.2M7 7v10h10" />
        </svg>
      );
    }
    return null;
  };

  return (
    <div className={`p-4 rounded-lg border ${getColorClasses(color)} ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium">{title}</p>
          <div className="flex items-baseline space-x-2">
            <p className="text-2xl font-bold">
              {typeof value === 'number' ? value.toLocaleString() : value}
            </p>
            {trend !== undefined && (
              <div className="flex items-center">
                {getTrendIcon(trend)}
                <span className={`text-xs font-medium ml-1 ${
                  trend > 0 ? 'text-green-500' : trend < 0 ? 'text-red-500' : 'text-gray-500'
                }`}>
                  {Math.abs(trend)}%
                </span>
              </div>
            )}
          </div>
          {subtitle && (
            <p className="text-xs text-gray-500 mt-1">{subtitle}</p>
          )}
        </div>
        <div className="text-2xl ml-4">{icon}</div>
      </div>
    </div>
  );
};

export default StatCard;
