#!/bin/sh
# Pod-local crons.yml change watcher: runs every minute from the same crontab.
# When the live .emergent/crons.yml hash differs from the last-applied hash it
# asks agent-service to reconcile PREVIEW crons (scope=preview keeps prod/AWS
# untouched). It never writes applied.hash (the install does, so a failed
# reconcile retries next minute) and never exits non-zero.
set -u

YAML="${CRONS_YAML_FILE:-/app/.emergent/crons.yml}"
APPLIED="${APPLIED_HASH_FILE:-/app/.emergent/cron/applied.hash}"
JOB_ID="${JOB_ID:-}"
CRON_API_URL="${CRON_API_URL:-}"

# sha256 of $1, or empty when the file is absent (matches the install writer).
hash_file() {
	if [ -f "$1" ]; then
		sha256sum "$1" 2>/dev/null | cut -d' ' -f1
	else
		printf ''
	fi
}

current="$(hash_file "$YAML")"
applied="$(cat "$APPLIED" 2>/dev/null || printf '')"

# Converged: nothing to do.
[ "$current" = "$applied" ] && exit 0
# No API URL baked (older pod) — can't reconcile; retry once one is present.
[ -n "$CRON_API_URL" ] || exit 0

# Fire-and-forget preview reconcile; silent on any transport failure.
curl -sS -o /dev/null --max-time 15 \
	-X POST \
	-H "Content-Type: application/json" \
	-d "{\"job_id\":\"$JOB_ID\",\"scope\":\"preview\"}" \
	"$CRON_API_URL/internal/crons/reconcile" >/dev/null 2>&1 || true
exit 0
