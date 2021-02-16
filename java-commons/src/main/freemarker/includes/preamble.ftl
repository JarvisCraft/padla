<#assign
__primitiveTypes=['boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double']
primitiveTypes=['int', 'long', 'double']
rootPackage="ru.progrm_jarvis.javacommons"
rootPackagePath="${rootPackage?replace('.', '/')}"
>
<#-- Basic macro for specialized primitive generation -->
<#macro generatePrimitiveSpecialization subpackage templateName classNamePrefix primitiveType classNameSuffix>
<#-- Definitions of the basic attributes -->
    <#assign
    capitalizedPrimitiveType="${primitiveType?cap_first}"
    className="${classNamePrefix}${capitalizedPrimitiveType}${classNameSuffix}"
    packageName="${rootPackage}.${subpackage}"
    packagePath="${rootPackagePath}/${subpackage?replace('.', '/')}"
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
    <#list primitiveTypes as primitiveType>
        <@generatePrimitiveSpecialization subpackage templateName classNamePrefix primitiveType classNameSuffix />
    </#list>
</#macro>
