require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name          = package["name"]
  s.version       = package["version"]
  s.summary       = package["description"]

  s.homepage      = package["repository"]["url"]
  s.license       = 'MIT'
  s.author        = package["author"]
  s.source        = { :git => package["repository"]["url"], :tag => "v#{s.version}" }  

  s.requires_arc   = true
  s.swift_version  = '5.0'
  s.platform       = :ios, "13.0"
  
  s.source_files = "ios/**/*.{swift,h,m}"

  s.dependency 'React'
  s.dependency 'TOTVSCameraKit'
  
end
