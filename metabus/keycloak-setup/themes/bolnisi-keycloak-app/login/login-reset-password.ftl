<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayMessage=!messagesPerField.existsError('username'); section>
    <#if section = "header-custom">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">
        <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <#if auth?has_content && auth.showUsername()>
                    <input type="email" id="username" placeholder="${msg('email')}" name="username" class="${properties.kcInputClass!} " autofocus value="${auth.attemptedUsername}" aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                <#else>
                    <input type="email" id="username" placeholder="${msg('email')}" name="username" class="${properties.kcInputClass!} " autofocus aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                </#if>

                <#if messagesPerField.existsError('username')>
                    <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.get('username'))?no_esc}
                    </span>
                </#if>
            </div>
            <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
                </div>
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}" style="text-align: center;margin-top: 25px;">
                        <span><a href="${url.loginUrl}" style="font-weight: 500;color: #1D439B;"><u>${kcSanitize(msg("backToLogin"))?no_esc}</u></a></span>
                    </div>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>