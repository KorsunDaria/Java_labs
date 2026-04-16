#!/bin/bash

# Имя выходного файла
OUTPUT="all_files_merged.txt"

# Очищаем или создаём выходной файл
> "$OUTPUT"

# Рекурсивно находим все файлы (кроме самого OUTPUT)
find . -type f ! -name "$OUTPUT" | while read -r file; do
    echo "Обработка: $file"

    # Добавляем заголовок с именем файла
    echo "========================================" >> "$OUTPUT"
    echo "Файл: $file" >> "$OUTPUT"
    echo "========================================" >> "$OUTPUT"
    echo "" >> "$OUTPUT"

    # Добавляем содержимое файла (если это текстовый файл)
    if file "$file" | grep -q text; then
        cat "$file" >> "$OUTPUT" 2>/dev/null
    else
        echo "[Бинарный файл - содержимое не отображается]" >> "$OUTPUT"
    fi

    echo "" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
done

echo "Готово! Все файлы объединены в $OUTPUT"