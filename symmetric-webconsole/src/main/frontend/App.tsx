import router from 'Frontend/routes.js';
import { RouterProvider } from 'react-router-dom';


import { I18nProvider } from "@lingui/react";
import { i18n } from "@lingui/core";
//import { messages as messagesEn } from "./locales/en/messages.js";

i18n.activate("en");

export default function App() {
  return (
    <I18nProvider i18n={i18n}>
      <RouterProvider router={router} />
    </I18nProvider>
  );

}
