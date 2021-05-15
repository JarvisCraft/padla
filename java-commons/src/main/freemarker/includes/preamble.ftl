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

<#function wrapperTypeOf primitiveType>
    <#switch primitiveType>
        <#case 'boolean'>
            <#return "Boolean">
        <#case 'byte'>
            <#return "Byte">
        <#case 'short'>
            <#return "Short">
        <#case 'char'>
            <#return "Character">
        <#case 'int'>
            <#return "Integer">
        <#case 'long'>
            <#return "Long">
        <#case 'float'>
            <#return "Float">
        <#case 'double'>
            <#return "Double">
        <#default><#stop "Unknown primitive type: ${primitiveType}">
    </#switch>
</#function>

<#function isCommonPrimitiveType type>
    <#return commonPrimitiveTypes?seq_contains(type) />
</#function>
