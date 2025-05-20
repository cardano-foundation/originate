import Container from "@mui/material/Container";
import "./style.scss";
import { Trans, useTranslation } from "react-i18next";
import { SelectLanguage } from "../../components/SelectLanguage";
import { privacyURL } from "../../constants";
import { Link } from "react-router-dom";

// Note - all 3 of these pages right now have the exact same content;
// We were advised at some point that they could differ which is why there are 3 at 3 separate URLs.
function TermsConditionsAPI() {
  const { t } = useTranslation();

  return (
    <Container
      maxWidth={false}
      data-testid="viewPageTerms"
    >
      <div className="box-header-terms">
        <div
          data-testid="headerTerms"
          className="title"
        >
          {t("terms")}
        </div>
        <SelectLanguage isOnlySelect={true} />
      </div>
      <div className="box-content-terms">
        <p>
          <strong>{t("termsTopText")}</strong>
        </p>

        <p className="title-desc">1. {t("terms1")}</p>
        <ol type="a">
          <li>{<Trans i18nKey="terms1.1" />}</li>
          <li>{<Trans i18nKey="terms1.2" />}</li>
          <li>{<Trans i18nKey="terms1.3" />}</li>
          <li>{<Trans i18nKey="terms1.4" />}</li>
          <li>
            {
              <Trans
                i18nKey="terms1.5"
                components={[
                  <Link
                    target="_blank"
                    to={privacyURL}
                  />,
                ]}
              />
            }
          </li>
          <li>{<Trans i18nKey="terms1.6" />}</li>
          <li>{<Trans i18nKey="terms1.7" />}</li>
        </ol>

        <p className="title-desc">2. {t("terms2")}</p>
        <ol type="a">
          <li>{<Trans i18nKey="terms2.1" />}</li>
          <li>{<Trans i18nKey="terms2.2" />}</li>
          <li>{<Trans i18nKey="terms2.3" />}</li>
          <li>{<Trans i18nKey="terms2.4" />}</li>
          <ul>
            <li>{t("terms2.4.1")}</li>
            <li>{t("terms2.4.2")}</li>
            <li>{t("terms2.4.3")}</li>
          </ul>
          <li>{<Trans i18nKey="terms2.5" />}</li>
        </ol>

        <p className="title-desc">3. {t("terms3")}</p>
        <ol type="a">
          <li>{<Trans i18nKey="terms3.1" />}</li>
          <li>{<Trans i18nKey="terms3.2" />}</li>
          <li>{<Trans i18nKey="terms3.3" />}</li>
          <li>{<Trans i18nKey="terms3.4" />}</li>
        </ol>

        <p className="title-desc">4. {t("terms4")}</p>
        <p>{t("terms4Header")}</p>
        <ol type="a">
          <li>{<Trans i18nKey="terms4.1" />}</li>
          <li>{<Trans i18nKey="terms4.2" />}</li>
          <li>{<Trans i18nKey="terms4.3" />}</li>
          <li>{<Trans i18nKey="terms4.4" />}</li>
        </ol>

        <p className="title-desc">5. {t("terms5")}</p>
        <p>{t("terms5.1")}</p>
        <p>{t("terms5.2")}</p>

        <p className="title-desc">6. {t("terms6")}</p>
        <ol type="a">
          <li>{<Trans i18nKey="terms6.1" />}</li>
          <li>{<Trans i18nKey="terms6.2" />}</li>
          <li>{<Trans i18nKey="terms6.3" />}</li>
          <li>{<Trans i18nKey="terms6.4" />}</li>
          <li>{<Trans i18nKey="terms6.5" />}</li>
          <li>{<Trans i18nKey="terms6.6" />}</li>
          <li>{<Trans i18nKey="terms6.7" />}</li>
          <li>{<Trans i18nKey="terms6.8" />}</li>
          <li>
            {<Trans i18nKey="terms6.9" />}
            <br />
            <br />
            Cardano Foundation,
            <br />
            Dammstrasse 16,
            <br />
            6300, Zug,
            <br />
            Switzerland
            <br />
            Attn: Legal
          </li>
        </ol>
      </div>
    </Container>
  );
}
export { TermsConditionsAPI };
