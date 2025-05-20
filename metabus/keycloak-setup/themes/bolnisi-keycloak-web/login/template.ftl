<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayWide=false>
    <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <html xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <link href="${url.resourcesPath}/css/styles.css" rel="stylesheet"/>
        <script src="${url.resourcesPath}/js/jquery-1.12.4.min.js"></script>
        <script src="${url.resourcesPath}/js/main.js"></script>

        <#if properties.meta?has_content>
            <#list properties.meta?split(' ') as meta>
                <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
            </#list>
        </#if>
        <title>Cardano Foundation | Bolnisi</title>
        <link rel="icon" href="${url.resourcesPath}/img/favicon.png"/>
        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}" rel="stylesheet"/>
            </#list>
        </#if>
        <#if properties.scripts?has_content>
            <#list properties.scripts?split(' ') as script>
                <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
            </#list>
        </#if>
        <#if scripts??>
            <#list scripts as script>
                <script src="${script}" type="text/javascript"></script>
            </#list>
        </#if>
    </head>

    <body class="${properties.kcBodyClass!} body-page" onload="loadingDefault()">
    <div id="language" style="display: none;">${msg("language")}</div>
    <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
        <select class="vodiapicker" id="kc-locale-wrapper-1">
            <#list locale.supported as l>
                <option value=${l.url} data-value=${l.url} data-thumbnail="${url.resourcesPath}/img/${l.languageTag}.svg"
                        data-path="${url.resourcesPath}">
                    ${l.label}
                </option>
            </#list>
        </select>
    </#if>

    <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
        <div id="kc-locale">
            <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                <div class="kc-dropdown" id="kc-locale-dropdown" style="margin-right: 20em">
                    <button class="btn-select" value="" style="background: var(--baseColor);padding-left: 10px;margin-top: 20px;"></button>
                    <div class="box-dropdown" id="show-dropdown">
                        <ul id="item"></ul>
                    </div>
                </div>
            </div>
        </div>
    </#if>
    <div class="${properties.kcLoginClass!}">
        <nav class="topNavbar">
            <img src="${url.resourcesPath}/img/logo.svg" class="img_size">
        </nav>

        <div class="${properties.kcFormCardClass!} <#if displayWide>${properties.kcFormCardAccountClass!}</#if>">
            <header class="${properties.kcFormHeaderClass!}">
                <h1 id="kc-page-title" style="width: 120%"><#nested "header"></h1>
            </header>
            <div id="kc-content">
                <div id="kc-content-wrapper">
                    <#if displayMessage && message?has_content>
                        <#if message.type != 'warning'>
                        <#--                        CONFIG ERROR TEXT ERROR-->
                        <div class="alert alert-${message.type}" style="display: flex;border-radius: 4px">
                            <#if message.type = 'success'><div><img
                                        src="${url.resourcesPath}/img/success.svg" class="icon_custom"
                                ></img></div></#if>
                            <#if message.type = 'error'><div><img
                                        src="${url.resourcesPath}/img/error.svg" class="icon_custom"
                                ></img></div></#if>
                            <#if message.type = 'info'><div
                                class="${properties.kcFeedbackInfoIcon!} icon"
                                data-value="alert"></div></#if>
                            <#if message?has_content>
                                    <div class="kc-feedback-text-${message.type}" style="padding: 5px 5px 5px 5px;line-height: 1.4;">${message.summary}</div>
                            </#if>
                        </div>
                        </#if>
                    </#if>
                    <#nested "form">
                    <#if displayInfo>
                        <div id="kc-info" class="${properties.kcSignUpClass!}">
                            <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                <#nested "info">
                            </div>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>
    <div class="footer" style="padding-right: 24px;padding-left: 24px">
        <div class="box-footer">
            <p  style="margin: 0 0 1px" class="title-footer">${msg("note-footer")}</p>
            <div class="box-link">
                <div>
                    <a onclick="changPage('${properties.url_front_end}'+'${msg("url_terms")}')"><u>${msg("terms-footer")}</u></a>
                </div>
                <div>
                    <a onclick="changPage('${properties.url_front_end}'+'${msg("url_policy")}')"><u>${msg("policy-footer")}</u></a>
                </div>
            </div>
        </div>
    </div>
    </body>
    </html>
</#macro>