import React from 'react';

// Mock Radix UI Select components for testing
export const Select = ({ children, onValueChange, ...props }: any) => {
  return (
    <div data-testid="select-mock" {...props}>
      {children}
    </div>
  );
};

export const SelectTrigger = React.forwardRef(({ children, id, ...props }: any, ref) => {
  return (
    <button
      ref={ref}
      type="button"
      role="combobox"
      id={id}
      aria-label={props['aria-label']}
      {...props}
    >
      {children}
    </button>
  );
});
SelectTrigger.displayName = 'SelectTrigger';

export const SelectValue = ({ placeholder }: any) => {
  return <span>{placeholder}</span>;
};

export const SelectContent = ({ children }: any) => {
  return <div role="listbox">{children}</div>;
};

export const SelectItem = ({ children, value, ...props }: any) => {
  const handleClick = (e: React.MouseEvent) => {
    const select = e.currentTarget.closest('[data-testid="select-mock"]');
    if (select) {
      const onValueChange = (select as any).onValueChange;
      if (onValueChange) {
        onValueChange(value);
      }
    }
  };

  return (
    <div
      role="option"
      data-value={value}
      onClick={handleClick}
      {...props}
    >
      {children}
    </div>
  );
};
