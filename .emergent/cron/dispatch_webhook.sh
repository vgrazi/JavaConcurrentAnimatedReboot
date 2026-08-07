#!/bin/sh
# Pod-local webhook-cron dispatcher: one crontab line per enabled cron, run by
# crond inside the preview/env pod. The full endpoint URL is substituted at
# render time; this fires a single request with the .env secret and exits 0.
set -eu

: "${CRON_NAME:?}" "${METHOD:?}" "${ENDPOINT_URL_B64:?}"
JOB_ID="${JOB_ID:-}"
WEBHOOK_ENV_FILE="${WEBHOOK_ENV_FILE:-/app/backend/.env}"
AT_DATE="${AT_DATE:-}"
END_DATE="${END_DATE:-}"

# AT_DATE (one-time trigger): crond can't express the year, so the "M H D Mo *"
# line re-fires this minute every year. Fire only when the current UTC minute
# (first 16 chars of RFC3339) matches AT_DATE's minute.
if [ -n "$AT_DATE" ]; then
	now_min="$(date -u +%Y-%m-%dT%H:%M)"
	at_min="$(printf '%s' "$AT_DATE" | cut -c1-16)"
	[ "$now_min" = "$at_min" ] || exit 0
fi

# END_DATE (recurring cutoff): both sides use the same fixed %Y-%m-%dT%H:%M:%SZ
# layout, so comparing their digit-only forms numerically preserves chronological
# order. Stop firing once now is strictly past END_DATE.
if [ -n "$END_DATE" ]; then
	now_num="$(date -u +%Y%m%d%H%M%S)"
	end_num="$(printf '%s' "$END_DATE" | tr -cd '0-9')"
	[ "$now_num" -le "$end_num" ] || exit 0
fi

ENDPOINT="$(printf '%s' "$ENDPOINT_URL_B64" | base64 -d)"

strip_quotes() {
	# Strip a single matching pair of surrounding quotes.
	v="$1"
	case "$v" in
		\"*\") v="${v#\"}"; v="${v%\"}" ;;
		\'*\') v="${v#\'}"; v="${v%\'}" ;;
	esac
	printf '%s' "$v"
}

# Read the per-app secret from the dotenv at dispatch time (never from cron env).
read_secret() {
	[ -f "$WEBHOOK_ENV_FILE" ] || return 0
	line="$(grep -E '^WEBHOOK_CRON_SECRET=' "$WEBHOOK_ENV_FILE" | tail -n 1 || true)"
	value="$(strip_quotes "${line#WEBHOOK_CRON_SECRET=}")"
	printf '%s' "$value"
}
WEBHOOK_CRON_SECRET="$(read_secret)"

# RUN_ID is the idempotency key: cron name + fire time (minute granularity
# matches the schedule floor).
RUN_ID="${CRON_NAME}-$(date -u +%Y%m%dT%H%M)"
DISPATCH_TIME="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
ENVELOPE="{\"event\":\"schedule.triggered\",\"schedule_id\":\"$CRON_NAME\",\"run_id\":\"$RUN_ID\",\"dispatch_time\":\"$DISPATCH_TIME\",\"job_id\":\"$JOB_ID\",\"data\":null}"

# Fire-and-forget: one request, no retries, no run reporting. `|| true` keeps
# `set -e` happy on a curl transport failure (000, non-zero exit).
# --location-trusted: internal-cluster pods get a cross-host 307 to the
# internal.<preview-host>; the Bearer must survive that same-platform redirect.
HTTP_STATUS="$(curl -sS -o /dev/null -w '%{http_code}' \
	--max-time 10 \
	--location-trusted --max-redirs 2 \
	-X "$METHOD" \
	-H "Authorization: Bearer $WEBHOOK_CRON_SECRET" \
	-H "Content-Type: application/json" \
	-H "X-Webhook-Id: $RUN_ID" \
	-H "X-Webhook-Timestamp: $DISPATCH_TIME" \
	-d "$ENVELOPE" \
	"$ENDPOINT" 2>/dev/null || true)"

echo "dispatch complete (cron=$CRON_NAME http=${HTTP_STATUS:-000})"
exit 0
