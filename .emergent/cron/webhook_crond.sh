#!/bin/sh
# supervisord program entrypoint for the pod-local webhook-cron daemon.
#
# Runs in the FOREGROUND so supervisord supervises it; declared autostart=true
# so it comes back automatically on every pod resume. Before exec'ing the cron
# daemon it self-heals the live crontab from the persistent workspace copy
# (/app is a PVC, /etc/cron.d is not) so a freshly resumed pod schedules the
# last-rendered crons even before agent-service reconciles.
set -eu

CRON_DIR=/app/.emergent/cron
PERSIST="$CRON_DIR/webhook-crons"
CRON_D=/etc/cron.d/webhook-crons
DISPATCH="$CRON_DIR/dispatch_webhook.sh"
LOG=/var/log/webhook-cron.log

# Restore the live /etc/cron.d entry from the persistent copy when present.
if [ -f "$PERSIST" ]; then
	mkdir -p "$(dirname "$CRON_D")" 2>/dev/null || true
	cp "$PERSIST" "$CRON_D" 2>/dev/null || true
	chmod 0644 "$CRON_D" 2>/dev/null || true
fi
[ -f "$DISPATCH" ] && chmod 0755 "$DISPATCH" 2>/dev/null || true
touch "$LOG" 2>/dev/null || true

# If the base image predates the `cron` package, install it at runtime
# (best-effort; a failure falls through to the error below). Debian base + sudo.
if ! command -v cron >/dev/null 2>&1 && ! command -v crond >/dev/null 2>&1; then
	echo "webhook_crond: no cron daemon found, attempting runtime install" >&2
	if command -v apt-get >/dev/null 2>&1; then
		SUDO=""
		[ "$(id -u)" -eq 0 ] || SUDO="sudo"
		$SUDO apt-get update >/dev/null 2>&1 &&
			$SUDO apt-get install -y --no-install-recommends cron >/dev/null 2>&1 ||
			echo "webhook_crond: runtime cron install failed" >&2
	fi
fi

# Prefer Debian/cronie `cron` (-f foreground), fall back to busybox `crond`.
if command -v cron >/dev/null 2>&1; then
	exec cron -f -L 15
elif command -v crond >/dev/null 2>&1; then
	exec crond -f -l 8
fi
echo "webhook_crond: no cron daemon (cron/crond) installed in image" >&2
exit 127
