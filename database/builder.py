#!/usr/local/bin/python
# -*- coding: utf-8 -*-
"""The following script builds SQLite database from CSV files.
Output database file contains tasks for the Android location-based
game. The script was tested with Python 2.6.6 (win32) and OpenCV 2.4.0"""

__version__ = 1.0
__author__  = "Andrzej Wojtowicz"
__docformat__ = "javadoc"

import csv
import cv
import cv2
import shutil
import sqlite3

#_______________________________________

CSV_TYPES   = 'types.csv' # task types
CSV_TASKS   = 'tasks.csv' # list of tasks
CSV_ARCH_EL = 'arch_elements.csv' # photo-based tasks
CSV_PLACES  = 'places.csv' # GPS-based tasks
CSV_ANSWERS = 'answers.csv' # text-based tasks

BUILD_PATH  = './build/' # output directory
VIEWS_PATH  = './views/' # photos directory
DB_NAME     = 'database.db' # output file name

ASSETS_PATH = '../android-project/assets/' # path to Android project (that's where
                                           # should be copied the output file)

#_______________________________________

def connect_and_clear_db():
    """Creates new database file with proper Android stucture.
    @return (connection, cursor) - sqlite3.Connection and sqlite3.Cursor instances"""

    print 'Connecting to {0} ...'.format(DB_NAME)

    conn = sqlite3.connect(BUILD_PATH + DB_NAME)
    cur = conn.cursor()
    
    print 'Dropping tables...'
    
    for table in ['android_metadata', 'types', 'tasks', 'views', 'features', 'places', 'answers']:
        cur.execute('DROP TABLE IF EXISTS {0}'.format(table))
        
    print 'Creating tables...'
        
    for stmt in ['android_metadata (locale TEXT DEFAULT "pl_PL")',
                 'types     (_id INTEGER PRIMARY KEY, name TEXT)',
                 'views     (_id INTEGER PRIMARY KEY, task_id INTEGER, name TEXT, FOREIGN KEY(task_id) REFERENCES tasks(_id))',
                 'tasks     (_id INTEGER PRIMARY KEY, type_id INTEGER, text_before TEXT, text_after TEXT, img_before TEXT, img_after TEXT, FOREIGN KEY(type_id) REFERENCES types(_id))',
                 'features  (_id INTEGER PRIMARY KEY, view_id INTEGER, pt_x REAL, pt_y REAL, desc TEXT, FOREIGN KEY(view_id) REFERENCES views(_id))',
                 'places    (_id INTEGER PRIMARY KEY, task_id INTEGER, gps_latitude REAL, gps_longitude REAL, min_distance REAL, theta_sight REAL, FOREIGN KEY(task_id) REFERENCES tasks(_id))',
                 'answers   (_id INTEGER PRIMARY KEY, task_id INTEGER, text TEXT, FOREIGN KEY(task_id) REFERENCES tasks(_id))']:
        cur.execute('CREATE TABLE {0}'.format(stmt))
        
    print 'Structure of the database prepared.'
    
    return conn, cur
    
def add_android_metadata(conn, cur):
    """Adds metadata to database file in order to allow Android read the data.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor"""

    print 'Adding Android metadata...',
    
    cur.execute('INSERT INTO android_metadata VALUES ("pl_PL")')
    
    conn.commit()
    
    print 'done.'
    
def add_types(conn, cur, types):
    """Adds to the database a types of tasks which can occure in the game. Types are read from CSV file.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor
    @param types"""

    print 'Adding types...',
    
    for type in types:
        cur.execute('INSERT INTO types VALUES ("{_id}", "{name}")'.format(
                _id  = type['id'],
                name = type['name'],
                )
            )
    
    conn.commit()
    
    print 'done.'
    
def add_tasks(conn, cur, tasks):
    """Adds to the database a list of tasks.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor
    @param tasks list of tasks read from CSV file"""
    
    print 'Adding tasks...',
    
    for task in tasks:
        cur.execute('INSERT INTO tasks VALUES ("{_id}", "{type_id}", "{text_before}", "{text_after}", "{img_before}", "{img_after}")'.format(
                _id         = task['id'],
                type_id     = task['type'],
                text_before = task['text_before'],
                text_after  = task['text_after'],
                img_before  = task['img_before'],
                img_after   = task['img_after']
                )
            )
            
    conn.commit()
            
    print 'done.'
    
def add_architectural_elements(conn, cur, objects):
    """Adds to the database a list of objects for photo-based tasks.
    From objects (concretely, it's a lists of pictures) are extracted SIFT keypoints and features.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor
    @param objects list of objects (photos) read from CSV file."""
    
    print 'Adding architectural elements...',
    
    sift_detector = cv2.FeatureDetector_create('SIFT')
    sift_descriptor = cv2.DescriptorExtractor_create('SIFT')
    
    feature_id = 1
    
    for i, object in enumerate(objects):
    
        cur.execute('INSERT INTO views VALUES ("{_id}", "{task_id}", "{name}")'.format(
                _id     = i+1,
                task_id = object['task_id'],
                name    = object['view_name'],
                )
            )
    
        img = cv2.imread(VIEWS_PATH + object['view_name'])
        
        keypoints = sift_detector.detect(img)
        features  = sift_descriptor.compute(img, keypoints)
        
        for keypoint, feature in zip(features[0], features[1]):
        
            cur.execute('INSERT INTO features VALUES ("{_id}", "{view_id}", "{pt_x}", "{pt_y}", "{desc}")'.format(
                    _id     = feature_id,
                    view_id = i+1,
                    pt_x    = keypoint.pt[0],
                    pt_y    = keypoint.pt[1],
                    desc    = str(list(feature))[1:-1].replace('.0', '').replace(' ', '')
                    )
                )
                
            feature_id += 1
    
    print 'done.'
    
    
def add_places(conn, cur, places):
    """Adds to the database a lists of places for GPS-based tasks.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor
    @param places list of places read from CSV file"""
    
    print 'Adding places...',
    
    for i, place in enumerate(places):
        cur.execute('INSERT INTO places VALUES ("{_id}", "{task_id}", "{gps_latitude}", "{gps_longitude}", "{min_distance}", "{theta_sight}")'.format(
                _id           = i+1,
                task_id       = place['task_id'],
                gps_latitude  = place['gps_latitude'],
                gps_longitude = place['gps_longitude'],
                min_distance  = place['min_distance'],
                theta_sight   = place['theta_sight']
                )
            )
            
    conn.commit()
            
    print 'done.'
    
    
def add_answers(conn, cur, answers):
    """Adds to the database a list of text-based tasks.
    @param conn instance of qlite3.Connection
    @param cur instance of qlite3.Cursor
    @param answers list of answers read from CSV file"""
    
    print 'Adding answers...',
    
    for i, answer in enumerate(answers):
        cur.execute('INSERT INTO answers VALUES ("{_id}", "{task_id}", "{text}")'.format(
                _id         = i+1,
                task_id     = answer['task_id'],
                text        = answer['text']
                )
            )
            
    conn.commit()
            
    print 'done.'
    
def copy_build():
    """Creates a copy of the database in Android project directory."""

    print 'Copying build file to Android assets directory...',

    src = BUILD_PATH + DB_NAME
    dst = ASSETS_PATH + DB_NAME
    
    shutil.copyfile(src, dst)
    
    print 'done.'
        
#_______________________________________

if __name__ == "__main__":

    types   = [i for i in csv.DictReader(open(CSV_TYPES,   'rb'), delimiter=';')]
    tasks   = [i for i in csv.DictReader(open(CSV_TASKS,   'rb'), delimiter=';')]
    arch_el = [i for i in csv.DictReader(open(CSV_ARCH_EL, 'rb'), delimiter=';')]
    places  = [i for i in csv.DictReader(open(CSV_PLACES,  'rb'), delimiter=';')]
    answers = [i for i in csv.DictReader(open(CSV_ANSWERS, 'rb'), delimiter=';')]

    conn, cur = connect_and_clear_db()
    
    add_android_metadata(conn, cur)
    add_types(conn, cur, types)
    add_tasks(conn, cur, tasks)
    add_architectural_elements(conn, cur, arch_el)
    add_places(conn, cur, places)
    add_answers(conn, cur, answers)
    
    cur.close()
    
    copy_build()