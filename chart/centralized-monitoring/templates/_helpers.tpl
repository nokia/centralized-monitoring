{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "centralizedMonitoring.name" -}}
{{- default .Chart.Name .Values.centralizedMonitoring.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "centralizedMonitoring.datasources" -}}
{{- printf "%s-%s" .Release.Name "datasources" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "centralizedMonitoring.fullname" -}}
{{- if .Values.centralizedMonitoring.fullnameOverride -}}
{{- .Values.centralizedMonitoring.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.centralizedMonitoring.nameOverride -}}
{{- if contains "$name" ".Release.Name" -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "centralizedMonitoring.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "centralizedMonitoring.labels" -}}
chart: {{ include "centralizedMonitoring.chart" . }}
{{ include "centralizedMonitoring.selectorLabels" . }}
{{- if .Chart.AppVersion }}
version: {{ .Chart.AppVersion | quote }}
{{- end }}
managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "centralizedMonitoring.selectorLabels" -}}
name: {{ include "centralizedMonitoring.name" . }}
instance: {{ .Release.Name }}
{{- end -}}