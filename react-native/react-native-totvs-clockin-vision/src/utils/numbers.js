
/**
 * return true iff value is either null or undefined 
 * 
 * @param {Object} value 
 */
export const isAbsent = value => null === value || undefined == value;

/**
 * Sanitize a value to a pure number or null 
 */
export const toFiniteFloatOrNull = value => {
  const isNumeric = value => !isNaN(value) && isFinite(value);
  try { 
    const e = parseFloat(value);
    return isNumeric(e) ? e : null;
  } catch (e) { return null; }
};