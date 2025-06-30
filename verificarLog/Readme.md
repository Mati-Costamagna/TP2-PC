# Sistema de Validación de Políticas

Este sistema analiza archivos de log de estadísticas y realiza validaciones automáticas para verificar la correcta implementación de políticas de complejidad.

## Validaciones Implementadas

### 1. **Entrada y Salida Total** ✅
Verifica la jerarquía de flujo principal: `0 ≥ 1 ≥ 11`
- **Éxito**: Los tres números son iguales
- **Warning**: No son iguales pero respetan la jerarquía
- **Error**: No se respeta la jerarquía

### 2. **Complejidad Simple** ✅
Verifica la jerarquía: `5 ≥ 6`
- **Éxito**: Ambos números son iguales
- **Warning**: No son iguales pero respetan la jerarquía
- **Error**: No se respeta la jerarquía

### 3. **Complejidad Media** ✅
Verifica la jerarquía: `2 ≥ 3 ≥ 4`
- **Éxito**: Los tres números son iguales
- **Warning**: No son iguales pero respetan la jerarquía
- **Error**: No se respeta la jerarquía

### 4. **Complejidad Alta** ✅
Verifica la jerarquía: `7 ≥ 8 ≥ 9 ≥ 10`
- **Éxito**: Los cuatro números son iguales
- **Warning**: No son iguales pero respetan la jerarquía
- **Error**: No se respeta la jerarquía

### 5. **Suma de Caminos** ✅
Verifica la jerarquía de distribución: `1 ≥ (2 + 5 + 7)`
- **Éxito**: La suma es exactamente igual al número `0`
- **Warning**: La suma no es igual a `0` pero respeta la jerarquía
- **Error**: No se respeta la jerarquía

## Expresiones Regulares Analizadas

- **Simple**: `0 1 5 6 11`
- **Media**: `0 1 2 3 4 11`
- **Compleja**: `0 1 7 8 9 10 11`

## Salida

El sistema genera archivos con timestamp en la carpeta `validaciones/` que incluyen:
- El archivo original completo
- Resultados de matches de regex
- Conteo individual de cada número (0-11)
- Resultados de todas las validaciones
- Información de fecha y metadatos
