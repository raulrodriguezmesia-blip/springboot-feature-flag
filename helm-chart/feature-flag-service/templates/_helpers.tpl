{{-/*
Create the name of the service account to use
*/}}
{{- define "feature-flag-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (printf "%s-%s" .Release.Name .Values.serviceAccount.name) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{-/*
Common labels
*/}}
{{- define "feature-flag-service.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{-/*
Selector labels
*/}}
{{- define "feature-flag-service.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{-/*
Full name
*/}}
{{- define "feature-flag-service.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- end }}