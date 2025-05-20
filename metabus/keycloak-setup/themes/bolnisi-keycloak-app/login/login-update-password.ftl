<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false; section>
    <#if section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">

        <div class="divider"></div>
        <div class="kcform">
            <form action="${url.loginAction}" method="post"  style="margin-top: 20px">
                <input type="email" id="username" name="username" value="${username}" autocomplete="email"
                       readonly="readonly" style="display:none;"/>
                <input type="password" id="password" name="password" autocomplete="current-password"
                       style="display:none;"/>

                <div class="${properties.kcFormGroupClass!}">
                    <input tabindex="2" id="password-new" name="password-new" class="${properties.kcInputClass!}"
                           autofocus autocomplete="new-password"
                           type="password" placeholder="${msg("passwordNew")}" required/>
                </div>

                <div class="${properties.kcFormGroupClass!}" style="padding-top: 20px">

                    <input type="password" id="password-confirm" name="password-confirm"
                           class="${properties.kcInputClass!}" autocomplete="new-password" placeholder="${msg("passwordConfirm")}" required/>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input tabindex="4"
                           class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doSubmit")}"/>
                </div>
            </form>
        </div>
    </#if>
</@layout.registrationLayout>