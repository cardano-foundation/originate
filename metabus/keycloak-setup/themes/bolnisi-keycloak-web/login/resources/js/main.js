function extractKcLocale(url) {
    var regex = /kc_locale=([^&]+)/;
    var match = url.match(regex);
    if (match && match[1]) {
        var kcLocaleValue = match[1];
        return kcLocaleValue.split("=")[0];
    } else {
        return "";
    }
}

function changUrl(url) {
    window.location.href = url
}

function extractKcLocaleCfproofoforigin(url) {
    const regex = /ui_locales=([^&]+)/;
    const match = url.match(regex);
    if (match && match[1]) {
        const kcLocaleValue = match[1];
        return kcLocaleValue.split("=")[0];
    } else {
        return "";
    }
}

function changeUrl(value) {
    window.location.href = value
}

function changPage(url) {
    let title = document.getElementById("language");
    let langChang = title.innerText;
    let urlPage
    if (langChang) {
        urlPage = url + "?kc_locale=" + langChang
    } else {
        urlPage = url;
    }
    window.open(urlPage, '_blank').focus();
}

function loadingDefault() {
    const url = decodeURIComponent(window.location.href);
    const u = document.getElementById("kc-locale-wrapper-1")
    if (!u || !extractKcLocaleCfproofoforigin(url)) {
        return;
    }
    let lang = extractKcLocaleCfproofoforigin(url);
    let urlLoading;
    document
        .querySelectorAll('option[data-value]')
        .forEach(elmNode => {
                if (lang === extractKcLocale(elmNode.value)) {
                    urlLoading = elmNode.value;
                }
            }
        );
    changeUrl(urlLoading)
}

$(document).ready(function () {
    $(function () {
        let title = document.getElementById("language");
        if(!title){
            return
        }
        let langChang = title.innerText;
        let itemCheck;
        //test for iterating over child elements
        let langArray = [];
        let langMapUrl = [];
        $('.vodiapicker option').each(function () {
            let img = $(this).attr("data-thumbnail");
            let path = $(this).attr("data-path")
            let text = this.innerText;
            let value = $(this).val();
            let iconNoCheck = path + "/img/icon_no_check.svg";
            let iconCheck = path + "/img/icon_check.svg";
            let iconDown = path + "/img/icon_down.svg";
            let item = '<li><div style="display: flex; align-items: center;justify-content: space-between">' +
                '<div style="display: flex; align-items: center;gap: 10px">' +
                '<img style="width: 30px;height: 30px;" src="' + img + '" alt="" value="' + value + '"/>' +
                '<p style="margin: 0">' + text + '</p> ' +
                '</div>' +
                '<img src="' + iconNoCheck + '" class="img-check"/></div></li>';
            if (langChang === extractKcLocale(value)) {
                itemCheck = '<li><div style="display: flex"><img style="width: 32px;height: 32px;" src="' + img + '" alt="" value="' + value + '"/>' +
                    '<img id="icon-expand" style="width: 24px;height: 30px;margin-left: 4px;margin-right: 2px;margin-top: 3px" src="' + iconDown + '"/></div></li>';
                item = '<li><div style="display: flex; align-items: center;justify-content: space-between">' +
                    '<div style="display: flex; align-items: center;gap: 10px">' +
                    '<img style="width: 30px;height: 30px;" src="' + img + '" alt="" value="' + value + '"/>' +
                    '<p style="margin: 0">' + text + '</p> ' +
                    '</div>' +
                    '<img src="' + iconCheck + '" class="img-check"/>' +
                    '</div>' +
                    '</li>'
            }
            langMapUrl.push({language: text.trim(), item: item})
        })

        langMapUrl.sort(function (a, b) {
            if (a.language < b.language) return -1;
            if (a.language > b.language) return 1;
            return 0
        })
        langMapUrl.forEach(value => {
            langArray.push(value.item)
        })
        $('#item').html(langArray);
        //Set the button value to the first el of the array
        $('.btn-select').html(itemCheck ? itemCheck : langArray[0]);
        $('.btn-select').attr('value', langChang);

        //change button stuff on click
        $('#item li').click(function () {
            let value = $(this).find('img').attr('value');
            changeUrl(value)
        });

        $(".btn-select").click(function () {
            let term = document.querySelectorAll(`[data-path]`);
            let iconUp = term[0].dataset.path + "/img/icon_up.svg";
            let iconDown = term[0].dataset.path + "/img/icon_down.svg";
            $(".box-dropdown").toggle();
            let id = document.getElementById("show-dropdown");
            if (id.style.display === "none") {
                id.style.display === 'block';
                $("#icon-expand").attr("src", iconDown)
            } else {
                id.style.display === 'none';
                $("#icon-expand").attr("src", iconUp)
            }
        });
    });
})
document.addEventListener('click', function (event) {
    const dropdown = document.getElementById('kc-locale-dropdown');
    const showDropdown = document.getElementById('show-dropdown');
    let term = document.querySelectorAll(`[data-path]`);
    let iconDown = term[0].dataset.path + "/img/icon_down.svg";
    if (!dropdown.contains(event.target)) {
        showDropdown.style.display = 'none';
        $("#icon-expand").attr("src", iconDown)
    }
});

// Add an event listener to the "select" button to toggle the display of the dropdown
document.getElementById('select')?.addEventListener('click', function () {
    const showDropdown = document.getElementById('show-dropdown');
    showDropdown.style.display = showDropdown.style.display === 'block' ? 'none' : 'block';
});