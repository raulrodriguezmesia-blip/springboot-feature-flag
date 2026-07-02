jobs:
  prepare-reports:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repo
        uses: actions/checkout@v4

      - name: Set quarter and year
        id: quarter
        run: |
          echo "quarter=Q2" >> $GITHUB_OUTPUT
          echo "year=2026" >> $GITHUB_OUTPUT

      - name: Prepare CTO report
        run: |
          cp reports/participation-${{ steps.quarter.outputs.quarter }}-${{ steps.quarter.outputs.year }}.md cto-report.md
          echo "CTO report prepared" > cto-report-${{ steps.quarter.outputs.quarter }}-${{ steps.quarter.outputs.year }}.pdf
