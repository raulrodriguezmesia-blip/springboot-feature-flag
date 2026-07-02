import sys
import os

def main():
    quarter = sys.argv[1]
    year = sys.argv[2]
    filename = f"reports/participation-{quarter}-{year}.md"

    os.makedirs("reports", exist_ok=True)
    with open(filename, "w") as f:
        f.write(f"# Participation Report {quarter} {year}\n")
        f.write("This is an auto-generated report.\n")
        f.write("- Metric A: 95%\n")
        f.write("- Metric B: 87%\n")
        f.write("- Metric C: 73%\n")

    print(f"Report generated: {filename}")

if __name__ == "__main__":
    main()

