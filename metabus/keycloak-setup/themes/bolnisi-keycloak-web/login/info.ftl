<#import "template.ftl" as layout>
<link href="${url.resourcesPath}/css/info.css" rel="stylesheet"/>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#else>
            <#if message.summary==msg('confirmExecutionOfActions')>${msg('actionRequired')}
            <#elseif message.summary==msg('accountUpdatedMessage')>${msg('successfullyUpdated')}
            <#else>
                ${message.summary}</#if>
        </#if>
    <#elseif section = "form">
        <div id="kc-info-message">
            <div>
                <#if message.summary==msg('confirmExecutionOfActions')>
                    <p class="instruction text_lable">${message.summary}<#if requiredActions??>:<div><#list requiredActions> <b><#items as reqActionItem>${kcSanitize(msg("requiredAction.${reqActionItem}"))?no_esc}<#sep>, </#items></b></#list><#else></#if></p></div>
                <#elseif message.summary==msg('accountUpdatedMessage')>
                    <p class="instruction text_lable">${message.summary}<#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${kcSanitize(msg("requiredAction.${reqActionItem}"))?no_esc}<#sep>, </#items></b></#list><#else></#if></p>
                <#else >
                    <p class="instruction text_lable">${message.summary}<#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${kcSanitize(msg("requiredAction.${reqActionItem}"))?no_esc}<#sep>, </#items></b></#list><#else></#if></p>
                </#if>
            </div>
            <div  id="kc-form-buttons">
            <#if skipLink??>
                <input readonly="readonly" style="cursor: pointer; text-align: center;" tabindex="4" onclick="changUrl('${properties.url_front_end}'+'${properties.fond_end_login}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("backToApplication"))?no_esc}"></input>
            <#else>
                <#if pageRedirectUri?has_content>
                    <input readonly="readonly" style="cursor: pointer; text-align: center;" tabindex="4" onclick="changUrl('${pageRedirectUri}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("backToApplication"))?no_esc}"></input>
                <#elseif actionUri?has_content>
                    <input readonly="readonly" style="cursor: pointer; text-align: center;" tabindex="4" onclick="changUrl('${actionUri}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("proceedWithAction"))?no_esc}"></input>
                <#elseif (client.baseUrl)?has_content>
                    <input readonly="readonly" style="cursor: pointer; text-align: center;" tabindex="4" onclick="changUrl('${client.baseUrl}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("backToApplication"))?no_esc}"></input>
                </#if>
            </#if>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>