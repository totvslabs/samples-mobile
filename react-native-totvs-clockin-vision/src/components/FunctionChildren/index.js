/**
 * Render appropriately a child component based on the type. 
 * We use this in case we need to have a child function component that 
 * need access to the camera component.
 * 
 * Bear in mind that if `children` is a function child, then it must return a 
 * react component.
 */
export default FunctionChildren = ({ camera, ...props }) => {
  const isFunction = e => typeof e === 'function';

  const { children, ...rest } = props;
  
  return isFunction(children) ? children({ camera, ...props }) : children;
}