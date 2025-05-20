<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("pageExpiredTitle")}
    <#elseif section = "form">
        <p class="text_lable">${msg("text-page-has-expired")}</p>
        <div style="margin-bottom: 25px;margin-top: 25px;">
            <input readonly="readonly" style="cursor: pointer; text-align: center;" tabindex="4"
                   onclick="changUrl('${url.loginRestartFlowUrl}')"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                   value="${kcSanitize(msg("continueLogin"))?no_esc}"></input>
        </div>
        <div>
            <input readonly="readonly"
                   style="cursor: pointer; text-align: center;border: 1px;color: black !important;background: var(--baseColor) !important;border: 1px solid #CCCCCC;"
                   tabindex="4" onclick="changUrl('${url.loginAction}')"
                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                   value="${kcSanitize(msg("restartLogin"))?no_esc}"></input>
        </div>
    </#if>
</@layout.registrationLayout>