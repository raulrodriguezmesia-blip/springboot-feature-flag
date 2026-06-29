{{/* Labels comunes para todos los recursos */}}
{{- define "springboot-feature-flag.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | default .Chart.Version }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* Nombre completo (fullname) para recursos */}}
{{- define "springboot-feature-flag.fullname" -}}
{{- if .Values.fullnameOverride }}
{{ .Values.fullnameOverride }}
{{- else }}
{{-  := default .Chart.Name .Values.nameOverride }}
{{- printf "%%s-%%s" .Release.Name  | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
