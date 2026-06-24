-- Drops blocks between `<!-- exclude:start -->` and `<!-- exclude:end -->`
-- markers when generating the bundled HTML documentation. The markers are
-- HTML comments, so they stay invisible on the GitHub-rendered README.

local function is_marker(block, name)
  return block.t == "RawBlock"
    and block.format == "html"
    and block.text:match("^%s*<!%-%-%s*exclude:" .. name .. "%s*%-%->%s*$") ~= nil
end

function Pandoc(doc)
  local kept = {}
  local excluding = false
  for _, block in ipairs(doc.blocks) do
    if not excluding and is_marker(block, "start") then
      excluding = true
    elseif excluding and is_marker(block, "end") then
      excluding = false
    elseif not excluding then
      kept[#kept + 1] = block
    end
  end
  doc.blocks = kept
  return doc
end
