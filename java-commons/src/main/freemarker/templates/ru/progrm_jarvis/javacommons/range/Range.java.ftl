<@pp.dropOutputFile />
<#import "/@includes/primitiveSpecializationGenerator.ftl" as generator>

<@generator.generateNumericSpecializations
subpackage="range" templateName="PrimitiveRangeBase"
classNamePrefix="" classNameSuffix="Range"
/>
