<#import '/@includes/preamble.ftl' as preamble />
<#-- Basic macro for specialized primitive generation -->
<#macro generatePrimitiveSpecialization subpackage templateName classNamePrefix primitiveType classNameSuffix>
<#-- Definitions of the basic attributes -->
    <#assign
    capitalizedPrimitiveType="${primitiveType?cap_first}"
    className="${classNamePrefix}${capitalizedPrimitiveType}${classNameSuffix}"
    packageName="${preamble.rootPackage}.${subpackage}"
    packagePath="${preamble.rootPackagePath}/${preamble.packageToPath(subpackage)}"
    >
<#--Specify the name of the target file -->
    <@pp.changeOutputFile name="/${packagePath}/${className}.java" />
<#-- Pick the correct primitive specialization-->
    <#include "${primitiveType}PrimitiveSpecializationDefinitions.ftl" />
<#-- Generate the class -->
    <#include "/@includes/${packagePath}/${templateName}.java.ftl">
</#macro>

<#-- Macro for universal specialized primitive generation -->
<#macro generatePrimitiveSpecializations subpackage templateName classNamePrefix classNameSuffix>
    <#list preamble.primitiveTypes as primitiveType>
        <@generatePrimitiveSpecialization subpackage templateName classNamePrefix primitiveType classNameSuffix />
    </#list>
</#macro>

<#-- Macro for specialized primitive generation for all types except boolean -->
<#macro generateNumericSpecializations subpackage templateName classNamePrefix classNameSuffix>
    <#list preamble.numericTypes as numericType>
        <@generatePrimitiveSpecialization subpackage templateName classNamePrefix numericType classNameSuffix />
    </#list>
</#macro>
