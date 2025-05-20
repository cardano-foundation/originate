<html>
<body>
${kcSanitize(msg("passwordResetBodyHtml",link, linkExpiration, realmName, linkExpirationFormatter(linkExpiration),user.username))?no_esc}
</body>
</html>