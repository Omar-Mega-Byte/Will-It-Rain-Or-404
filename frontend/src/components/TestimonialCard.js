import React from 'react';

const TestimonialCard = ({ name, role, image, quote, rating, className = '' }) => {
  return (
    <div className={`testimonial-card ${className}`}>
      <div className="testimonial-content">
        <div className="testimonial-stars">
          {[...Array(rating)].map((_, i) => (
            <span key={i}>⭐</span>
          ))}
        </div>
        <p className="testimonial-quote">"{quote}"</p>
        <div className="testimonial-author">
          <div className="author-avatar">{image}</div>
          <div className="author-info">
            <div className="author-name">{name}</div>
            <div className="author-role">{role}</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TestimonialCard;
