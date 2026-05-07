{{- define "futsal.labels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: futsal
{{- end -}}

{{- define "futsal.backend.labels" -}}
{{ include "futsal.labels" . }}
app.kubernetes.io/component: backend
app: backend
{{- end -}}

{{- define "futsal.frontend.labels" -}}
{{ include "futsal.labels" . }}
app.kubernetes.io/component: frontend
app: frontend
{{- end -}}

{{- define "futsal.backend.selectorLabels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: backend
{{- end -}}

{{- define "futsal.frontend.selectorLabels" -}}
app.kubernetes.io/name: futsal
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/component: frontend
{{- end -}}
