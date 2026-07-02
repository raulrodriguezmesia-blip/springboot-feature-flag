import sys
import os
from datetime import datetime

def main():
    # Recibe trimestre y año desde argumentos
    if len(sys.argv) < 3:
        print("Usage: python generate_report.py <quarter> <year>")
        sys.exit(1)

    quarter = sys.argv[1]
    year = sys.argv[2]

    # Nombre del archivo
    filename = f"reports/participation-{quarter}-{year}.md"

    # Crear carpeta si no existe
    os.makedirs("reports", exist_ok=True)

    # Contenido del reporte (ejemplo con métricas ficticias)
    content = f"""# Participation Report {quarter} {year}

Este informe fue generado automáticamente.

## Métricas principales
- Asistencia promedio: 92%
- Participación activa: 78%
- Feedback positivo: 85%

## Observaciones
El trimestre {quarter} del {year} muestra una tendencia estable en la participación.
"""

    # Escribir archivo
    with open(filename, "w", encoding="utf-8") as f:
        f.write(content)

    print(f"✅ Reporte generado: {filename}")

if __name__ == "__main__":
    main()

