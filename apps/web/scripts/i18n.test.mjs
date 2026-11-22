import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const load = (language) =>
  JSON.parse(readFileSync(new URL(`../src/locales/${language}.json`, import.meta.url)))

function flatten(object, prefix = '') {
  return Object.entries(object).flatMap(([key, value]) => {
    const name = prefix ? `${prefix}.${key}` : key
    return typeof value === 'string' ? [[name, value]] : flatten(value, name)
  })
}

const placeholders = (text) =>
  [...text.matchAll(/{{\s*(\w+)\s*}}/g)].map((match) => match[1]).sort()

test('English and French contain matching keys and interpolation values', () => {
  const english = new Map(flatten(load('en')))
  const french = new Map(flatten(load('fr')))
  assert.deepEqual([...english.keys()].sort(), [...french.keys()].sort())
  for (const [key, value] of english) {
    assert.deepEqual(placeholders(value), placeholders(french.get(key)), key)
  }
})
