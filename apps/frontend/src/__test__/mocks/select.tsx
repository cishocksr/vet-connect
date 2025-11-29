import React from 'react';

// Mock Radix UI Select components for testing
interface SelectProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  onValueChange?: (value: string) => void;
}

export const Select = ({ children, onValueChange, ...props }: SelectProps) => {
  return (
    <div data-testid="select-mock" {...props} onValueChange={onValueChange}>
      {children}
    </div>
  );
};

interface SelectTriggerProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  id?: string;
  'aria-label'?: string;
}

export const SelectTrigger = React.forwardRef(({ children, id, ...props }: SelectTriggerProps, ref: React.Ref<HTMLButtonElement>) => {
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

interface SelectValueProps {
  placeholder?: string;
}

export const SelectValue = ({ placeholder }: SelectValueProps) => {
  return <span>{placeholder}</span>;
};

interface SelectContentProps {
  children: React.ReactNode;
}

export const SelectContent = ({ children }: SelectContentProps) => {
  return <div role="listbox">{children}</div>;
};

interface SelectItemProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  value: string;
}

export const SelectItem = ({ children, value, ...props }: SelectItemProps) => {
  const handleClick = (e: React.MouseEvent) => {
    const select = e.currentTarget.closest('[data-testid="select-mock"]');
    if (select) {
      interface CustomSelectElement extends HTMLDivElement {
        onValueChange?: (value: string) => void;
      }
      const typedSelect = select as CustomSelectElement;
      const onValueChange = typedSelect.onValueChange;
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
