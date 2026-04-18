import { Suspense } from "react";
import PublicFormExperience from "../../components/PublicFormExperience";

export default function FormPage() {
  return (
    <Suspense fallback={<div className="public-form-loading">Carregando formulario...</div>}>
      <PublicFormExperience />
    </Suspense>
  );
}
