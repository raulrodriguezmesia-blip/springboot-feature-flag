jobs:
  prepare-reports:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repo
        uses: actions/checkout@v3

      - name: Ensure reports folder exists
        run: mkdir -p reports

      - name: Generate quarterly report if missing
        run: |
          if [ ! -f reports/participation-Q-2026.md ]; then
            echo "# Quarterly Participation Report" > reports/participation-Q-2026.md
            echo "Generated automatically as fallback." >> reports/participation-Q-2026.md
          fi

      - name: Copy report for CTO
        run: cp reports/participation-Q-2026.md cto-report.md
