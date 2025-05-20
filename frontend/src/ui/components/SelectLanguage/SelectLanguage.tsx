import { useEffect, useState, useRef } from "react";
import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
} from "@mui/material";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { i18n } from "../../../i18n";
import { LanguageType } from "./types";
import { IconExpand } from "../../assets/icons/iconExpandDown";
import { FlagEnglish, FlagGeorgian } from "../../assets/icons/IconFlagLanguage";
import { IconNotSelected, IconSelected } from "../../assets/icons/IconSelected";
import "./style.scss";
import useWindowSize from "../../hooks/useWindowSize";

interface IProps {
  isOnlySelect?: boolean;
}

const SelectLanguage = ({ isOnlySelect }: IProps) => {
  const { t } = useTranslation();
  const history = useHistory();
  const [lang, setLang] = useState<string>(() => {
    const storedLang = window.localStorage.getItem("language");
    if (storedLang) {
      return storedLang === LanguageType.English
        ? LanguageType.English
        : LanguageType.Georgian;
    } else {
      // @TODO post-pilot - foconnor: Ideally wouldn't store this now so change in browser lang would be reflected
      // but currently have issues with Keycloak language detection so right now this is easiest for pilot.
      const toStore = navigator.language.startsWith(LanguageType.English)
        ? LanguageType.English
        : LanguageType.Georgian;
      window.localStorage.setItem("language", toStore);
      return toStore;
    }
  });
  const windowSize = useWindowSize();

  const queryParams = new URLSearchParams(history?.location?.search);
  const locale = queryParams.get("kc_locale");
  const isSelectChangeLanguage = useRef(false);
  const elementSelect = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (
      (locale === LanguageType.English || locale === LanguageType.Georgian) &&
      !isSelectChangeLanguage.current
    ) {
      i18n.changeLanguage(locale);
      window.localStorage.setItem("language", locale);
      setLang(locale);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [history?.location?.search, locale, i18n.language]);

  const options = [
    {
      flag: FlagEnglish,
      label: t("english"),
      value: t("english"),
      valueType: LanguageType.English,
    },
    {
      flag: FlagGeorgian,
      label: t("georgian"),
      value: t("georgian"),
      valueType: LanguageType.Georgian,
    },
  ];

  const handleChangeLanguage = (lang: string) => {
    const language =
      lang === t("english") ? LanguageType.English : LanguageType.Georgian;
    i18n.changeLanguage(language);
    window.localStorage.setItem("language", language);
    isSelectChangeLanguage.current = true;
    setLang(language);
  };

  const positionOption = elementSelect.current?.getBoundingClientRect()
    ? windowSize.width - elementSelect.current?.getBoundingClientRect().right
    : 0;

  return (
    <>
      {isOnlySelect ? (
        <Select
          ref={elementSelect}
          value={lang === LanguageType.English ? t("english") : t("georgian")}
          onChange={(e: SelectChangeEvent<string>) =>
            handleChangeLanguage(e.target.value)
          }
          renderValue={(selected) => (
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                fontWeight: 500,
              }}
            >
              {selected === t("english") ? <FlagEnglish /> : <FlagGeorgian />}
            </Box>
          )}
          sx={{
            height: "52px",
            width: "80px",
          }}
          className="select-icon"
          IconComponent={(_props) => {
            const rotate = _props.className.toString().includes("iconOpen");
            return (
              <div
                style={{
                  position: "absolute",
                  cursor: "pointer",
                  pointerEvents: "none",
                  right: rotate ? 5 : 5,
                  top: rotate ? 10 : 15,
                  transform: rotate ? "rotate(180deg)" : "none",
                }}
              >
                <IconExpand />
              </div>
            );
          }}
          MenuProps={{
            sx: {
              ".MuiPaper-root": {
                right: `${positionOption}px`,
                left: "unset !important",
              },
            },
          }}
          data-testid="select-language"
        >
          {options &&
            options.length > 0 &&
            options.map((el) => (
              <MenuItem
                key={el.value}
                value={el.value}
                sx={{
                  fontWeight: "500",
                  minWidth: "350px",
                  height: "52px",
                  overflowY: "hidden",
                }}
                data-testid={el.valueType}
              >
                <Box className="select-content">
                  <Box className="select-label">
                    <el.flag />
                    <p>{el.label}</p>
                  </Box>
                  {lang === el.valueType ? (
                    <IconSelected />
                  ) : (
                    <IconNotSelected />
                  )}
                </Box>
              </MenuItem>
            ))}
        </Select>
      ) : (
        <Box>
          <FormControl className="select-language-container">
            <InputLabel id="lable-select-language">{t("language")}</InputLabel>
            <Select
              value={
                lang === LanguageType.English ? t("english") : t("georgian")
              }
              onChange={(e: SelectChangeEvent<string>) =>
                handleChangeLanguage(e.target.value)
              }
              renderValue={(selected) => (
                <Box
                  sx={{
                    display: "flex",
                    my: "10px",
                    mx: "20px",
                    alignItems: "center",
                    fontWeight: 500,
                  }}
                >
                  {selected === t("english") ? (
                    <FlagEnglish />
                  ) : (
                    <FlagGeorgian />
                  )}
                  <p className="language-selected">{selected}</p>
                </Box>
              )}
              sx={{
                height: "52px",
              }}
              IconComponent={(_props) => {
                const rotate = _props.className.toString().includes("iconOpen");
                return (
                  <div
                    style={{
                      position: "absolute",
                      cursor: "pointer",
                      pointerEvents: "none",
                      right: rotate ? 10 : 10,
                      top: rotate ? 10 : 15,
                      transform: rotate ? "rotate(180deg)" : "none",
                    }}
                  >
                    <IconExpand />
                  </div>
                );
              }}
              data-testid="select-language"
            >
              {options &&
                options.length > 0 &&
                options.map((el) => (
                  <MenuItem
                    key={el.value}
                    value={el.value}
                    sx={{
                      fontWeight: "500",
                      height: "52px",
                      overflowY: "hidden",
                    }}
                    data-testid={el.valueType}
                  >
                    <Box className="select-content">
                      <Box className="select-label">
                        <el.flag />
                        <p>{el.label}</p>
                      </Box>
                      {lang === el.valueType ? (
                        <IconSelected />
                      ) : (
                        <IconNotSelected />
                      )}
                    </Box>
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
        </Box>
      )}
    </>
  );
};

export { SelectLanguage };
