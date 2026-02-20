import React from 'react';
import './PrimaryButton.css';

const PrimaryButton = ({ children, className = '', ...props }) => {
  const combinedClassName = className ? `primary-button ${className}` : 'primary-button';

  return (
    <button className={combinedClassName} {...props}>
      {children}
    </button>
  );
};

export default PrimaryButton;
