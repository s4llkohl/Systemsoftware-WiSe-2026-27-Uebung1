import os
import re
import argparse
import time
import threading

def count_lines(file_path):
    with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
        line_count = sum(1 for line in file)
    return line_count

def count_lines_thread(file_list, results, thread_index):
    thread_lines = 0
    for file_path in file_list:
        thread_lines += count_lines(file_path)
    results[thread_index] = thread_lines

def count_lines_in_all_files(folder_path, regex_str, num_threads):
    total_lines = 0
    regex = re.compile(regex_str)


    all_files = []
    for root, dirs, files in os.walk(folder_path):
        for file_name in files:
            if regex.match(file_name):
                file_path = os.path.join(root, file_name)
                all_files.append(file_path)

    files_for_thread = []
    for i in range(num_threads):
        files_for_thread.append([])
    for i in range(len(all_files)):
        files_for_thread[i % num_threads].append(all_files[i])

    results = [0] * num_threads

    threads = []
    for i in range(num_threads):
        t = threading.Thread(target=count_lines_thread, args=(files_for_thread[i], results, i))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    for thread_lines in results:
        total_lines += thread_lines

    return total_lines

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Count lines in all files matching regex in a directory tree.'
    )
    parser.add_argument('folder_path', help='Path to the folder')
    parser.add_argument('regex_str', help='Regular expression string to match files')
    parser.add_argument('num_threads', type=int, help='Number of threads')  

    args = parser.parse_args()

    start_time = time.perf_counter()

    total_lines = count_lines_in_all_files(args.folder_path, args.regex_str, args.num_threads)

    end_time = time.perf_counter()

    print(f'Number of threads: {args.num_threads}')  
    print(f'Total number of lines: {total_lines}')
    print(f'Time taken: {(end_time - start_time) * 1000:.3f} ms')
