import React from 'react';

const FAQItem = ({ question, answer, isOpen, onToggle, className = '' }) => {
  return (
    <div className={`faq-item ${className}`}>
      <button
        className="faq-question"
        onClick={onToggle}
      >
        <span>{question}</span>
        <span className={`faq-icon ${isOpen ? 'open' : ''}`}>▼</span>
      </button>
      <div className={`faq-answer ${isOpen ? 'open' : ''}`}>
        <p>{answer}</p>
      </div>
    </div>
  );
};

export default FAQItem;
