<@pp.dropOutputFile />
<#import "/@includes/primitiveSpecializationGenerator.ftl" as generator>

<@generator.generatePrimitiveSpecializations
subpackage="util.function" templateName="PrimitiveBinaryOperatorBase"
classNamePrefix="" classNameSuffix="BinaryOperator"
/>
