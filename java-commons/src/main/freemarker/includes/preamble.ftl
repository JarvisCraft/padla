<#function packageToPath package>
    <#return package?replace('.', '/')>
</#function>
<#assign
<#-- Primitive types for which there are primitive specializations in JDK -->
commonPrimitiveTypes=['int', 'long', 'double']
<#-- Numeric primitive types -->
numericTypes=['byte', 'short', 'char', 'int', 'long', 'float', 'double']
<#-- Primitive types -->
primitiveTypes=['boolean', 'byte', 'short', 'char', 'int', 'long', 'float', 'double']
<#-- Root package of Java commons -->
rootPackage="ru.progrm_jarvis.javacommons"
rootPackagePath=packageToPath(rootPackage)
>

<#function isCommonPrimitiveType type>
    <#return commonPrimitiveTypes?seq_contains(type) />
</#function>

<#-- Basic macro for specialized primitive generation -->
<#macro generatePrimitiveSpecialization subpackage templateName classNamePrefix primitiveType classNameSuffix>
<#-- Definitions of the basic attributes -->
    <#assign
    capitalizedPrimitiveType="${primitiveType?cap_first}"
    className="${classNamePrefix}${capitalizedPrimitiveType}${classNameSuffix}"
    packageName="${rootPackage}.${subpackage}"
    packagePath="${rootPackagePath}/${packageToPath(subpackage)}"
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

<#-- Macro for specialized primitive generation for all types except boolean -->
<#macro generateNumericSpecializations subpackage templateName classNamePrefix classNameSuffix>
    <#list numericTypes as numericType>
        <@generatePrimitiveSpecialization subpackage templateName classNamePrefix numericType classNameSuffix />
    </#list>
</#macro>
