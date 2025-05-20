<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${kcSanitize(msg("errorTitle"))?no_esc}
    <#elseif section = "form">
        <div id="kc-error-message">
            <p style="font-weight: 500;color: #030321" class="instruction">${kcSanitize(message.summary)?no_esc}</p>
            <#if skipLink??>
                <input readonly="readonly" style="cursor: pointer; text-align: center;margin-top: 25px" tabindex="4" onclick="changUrl('${properties.url_front_end}'+'${properties.fond_end_login}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("backToApplication"))?no_esc}"></input>
            <#else>
                <#if client?? && client.baseUrl?has_content>
                    <input readonly="readonly" style="cursor: pointer; text-align: center;margin-top: 25px" tabindex="4" onclick="changUrl('${client.baseUrl}')" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" value="${kcSanitize(msg("backToApplication"))?no_esc}"></input>
                </#if>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>