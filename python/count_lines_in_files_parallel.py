import sys
import os
import re
import argparse
import time
from concurrent.futures import ThreadPoolExecutor

def count_lines(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count

def count_lines_in_all_files(folder_path, regex_str, num_threads):
    regex = re.compile(regex_str)
    num_threads = int(num_threads)
    file_paths = []

    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_paths.append(os.path.join(root, file_name))

    with ThreadPoolExecutor(max_workers=num_threads) as executor:
        results = executor.map(count_lines, file_paths)

        total_lines = sum(results)

    return total_lines

if __name__ == "__main__":


    if hasattr(sys, "_is_gil_enabled"):
        print("GIL enabled:", sys._is_gil_enabled())
    else:
        print("GIL status cannot be queried")
    
    parser = argparse.ArgumentParser(
        description='Count lines in all files matching regex in a directory tree.'
    )
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')
    parser.add_argument('num_threads', help='Number of Threads')
    args = parser.parse_args()

    start_time = time.perf_counter()

    total_lines = count_lines_in_all_files(args.folder_path, args.regex_str, args.num_threads)

    end_time = time.perf_counter()

    print(f'Total number of lines: {total_lines}')
    print(f'Time taken: {(end_time - start_time) * 1000:.3f} ms')


