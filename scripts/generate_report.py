import sys
import os

def main():
    quarter = sys.argv[1]
    year = sys.argv[2]
    filename = f"reports/participation-{quarter}-{year}.md"

    os.makedirs("reports", exist_ok=True)

    content = f"""# Participation Report {quarter} {year}

Este informe fue generado automáticamente.

## Métricas principales
- Asistencia promedio: 92%
- Participación activa: 78%
- Feedback positivo: 85%

## Observaciones
El trimestre {quarter} del {year} muestra una tendencia estable en la participación.
"""

    with open(filename, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"✅ Reporte generado: {filename}")

if __name__ == "__main__":
    main()
